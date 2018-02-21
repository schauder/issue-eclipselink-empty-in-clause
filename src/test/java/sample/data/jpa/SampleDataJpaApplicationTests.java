package sample.data.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.ManagedType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Integration test to run the application.
 *
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SampleDataJpaApplication.class)
@Transactional
public class SampleDataJpaApplicationTests {

	@Autowired
	EntityManager em;

	@Test
	public void contextLoads() {
	}

	@Test
	public void storeSimpleEntity() {
		SimpleEntity entity = new SimpleEntity();
		entity.setId(23L);
		em.persist(entity);
		em.flush();
	}

	@Test
	public void storeEntityWithNestedIdClass() {

		Table2 t2 = new Table2();
		t2.id1 = "21";
		t2.id2 = "22";

		Table1 t1 = new Table1();
		t1.id = "21";
		t1.table2 = t2;

		em.persist(t1);
		em.flush();

	}


}
