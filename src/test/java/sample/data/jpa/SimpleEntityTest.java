package sample.data.jpa;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;


/**
 * Integration test to run the application.
 *
 * @author Oliver Gierke
 * @author Jens Schauder
 */
public class SimpleEntityTest {

	private static final String SELECT_WITH_IN_CLAUSE = "SELECT se FROM SimpleEntity se WHERE se.id IN :ids";
	private static final String SELECT_WITH_IN_CLAUSE_IN_PARENS = "SELECT se FROM SimpleEntity se WHERE se.id IN (:ids)";

	EntityManager em;


	@Before
	public void before() {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("simple");
		em = emf.createEntityManager();
		em.getTransaction().begin();

		storeEntity(23L);
		storeEntity(42L);
	}

	@After
	public void after() {

		em.getTransaction().rollback();
	}

	@Test
	public void testInALoop() {

		Map<String, Function<EntityManager, Query>> querySources = new HashMap<>();
		querySources.put("jpql no parens", SimpleEntityTest::createJpqlQuery);
		querySources.put("jpql parens   ", SimpleEntityTest::createJpqlQueryWithParens);
		querySources.put("criteria API  ", SimpleEntityTest::createQueryWithInClause);

		Map<String, ParameterConfig> parameterConfigs = new HashMap<>();
		parameterConfigs.put("empty col ", new ParameterConfig(Collections.emptyList(), a -> a.isEmpty()));
		parameterConfigs.put("single col", new ParameterConfig(Collections.singleton(23L), a -> a.hasSize(1)));

		SoftAssertions softly = new SoftAssertions();

		for (Map.Entry<String, Function<EntityManager, Query>> querySourceEntry : querySources.entrySet()) {
			for (Map.Entry<String, ParameterConfig> configEntry : parameterConfigs.entrySet()) {

				String description = configEntry.getKey() + " - " + querySourceEntry.getKey();
				try {
					List result = querySourceEntry.getValue().apply(em)
							.setParameter("ids", configEntry.getValue().value)
							.getResultList();

					AbstractListAssert listAssert = softly.assertThat(result).describedAs(description);

					configEntry.getValue().test.accept(listAssert);

				} catch (RuntimeException rex) {

					softly.fail(description + " " + rex.getMessage());
				}
			}
		}

		softly.assertAll();
	}

	private static class ParameterConfig {

		final Object value;
		final Consumer<AbstractListAssert> test;

		ParameterConfig(Object value, Consumer<AbstractListAssert> test) {

			this.value = value;
			this.test = test;
		}
	}

	private static Query createJpqlQuery(EntityManager em) {
		return em.createQuery(SELECT_WITH_IN_CLAUSE);
	}

	private static Query createJpqlQueryWithParens(EntityManager em) {
		return em.createQuery(SELECT_WITH_IN_CLAUSE_IN_PARENS);
	}

	// this one fails
	@Test
	public void inWithEmptyList() {

		List result = createJpqlQuery(em)
				.setParameter("ids", Collections.emptyList())
				.getResultList();

		assertThat(result).isEmpty();
	}

	@Test
	public void inCriteriaApiWithEmptyList() {

		Query query = createQueryWithInClause(em);

		List<Object> result = query
				.setParameter("ids", Collections.emptyList())
				.getResultList();

		assertThat(result).isEmpty();
	}

	@Test
	public void inCriteriaApiWithNonEmptyList() {

		Query query = createQueryWithInClause(em);

		List<Object> result = query
				.setParameter("ids", Collections.singleton(23L))
				.getResultList();

		assertThat(result).hasSize(1);
	}

	@Test
	public void inWithNonEmptyMatchingList() {

		List result = createJpqlQuery(em)
				.setParameter("ids", Collections.singleton(23L))
				.getResultList();

		assertThat(result).hasSize(1);
	}

	@Test
	public void inWithNonEmptyNonMatchingList() {

		List result = createJpqlQuery(em)
				.setParameter("ids", Collections.singleton(43L))
				.getResultList();

		assertThat(result).isEmpty();
	}

	private static Query createQueryWithInClause(EntityManager em) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object> query = cb.createQuery();
		Root<SimpleEntity> root = query.from(SimpleEntity.class);
		query.select(root).where(root.get("id").in(cb.parameter(Collection.class, "ids")));

		return em.createQuery(query);
	}

	private void storeEntity(long id) {

		SimpleEntity entity = new SimpleEntity();
		entity.setId(id);
		em.persist(entity);
		em.flush();
	}

}
