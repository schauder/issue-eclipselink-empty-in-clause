package sample.data.jpa;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to run the application.
 *
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@ComponentScan
@Transactional
public class SampleDataJpaApplicationTests {

	private static final String SELECT_WITH_IN_CLAUSE = "SELECT se FROM SimpleEntity se WHERE se.id IN :ids";

	@Autowired
	EntityManager em;


	@Before
	public void before() {
		storeEntity(23L);
		storeEntity(42L);

	}

	@Test
	public void contextLoads() {
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
