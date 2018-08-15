package sample.data.jpa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


/**
 * Integration test to run the application.
 *
 * @author Oliver Gierke
 * @author Jens Schauder
 */
public class SampleDataJpaApplicationTests {

	private static final String SELECT_WITH_IN_CLAUSE = "SELECT se FROM SimpleEntity se WHERE se.id IN :ids";

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

	// this one fails
	@Test
	public void inWithEmptyList() {

		List result = em.createQuery(SELECT_WITH_IN_CLAUSE)
				.setParameter("ids", Collections.emptyList())
				.getResultList();

		assertThat(result).isEmpty();
	}

	@Test
	public void inCriteriaApiWithEmptyList() {

		CriteriaQuery<Object> query = createQueryWithInClause();

		List<Object> result = em.createQuery(query)
				.setParameter("ids", Collections.emptyList())
				.getResultList();

		assertThat(result).isEmpty();
	}

	@Test
	public void inCriteriaApiWithNonEmptyList() {

		CriteriaQuery<Object> query = createQueryWithInClause();

		List<Object> result = em.createQuery(query)
				.setParameter("ids", Collections.singleton(23L))
				.getResultList();

		assertThat(result).hasSize(1);
	}

	@Test
	public void inWithNonEmptyMatchingList() {

		List result = em.createQuery(SELECT_WITH_IN_CLAUSE)
				.setParameter("ids", Collections.singleton(23L))
				.getResultList();

		assertThat(result).hasSize(1);
	}

	@Test
	public void inWithNonEmptyNonMatchingList() {

		List result = em.createQuery(SELECT_WITH_IN_CLAUSE)
				.setParameter("ids", Collections.singleton(43L))
				.getResultList();

		assertThat(result).isEmpty();
	}

	private CriteriaQuery<Object> createQueryWithInClause() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object> query = cb.createQuery();
		Root<SimpleEntity> root = query.from(SimpleEntity.class);
		query.select(root).where(root.get("id").in(cb.parameter(Collection.class, "ids")));

		return query;
	}

	private void storeEntity(long id) {

		SimpleEntity entity = new SimpleEntity();
		entity.setId(id);
		em.persist(entity);
		em.flush();
	}

}
