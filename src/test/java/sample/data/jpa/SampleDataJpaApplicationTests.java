package sample.data.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import static org.junit.Assert.*;
import static org.springframework.data.jpa.repository.support.JpaEntityInformationSupport.*;

/**
 * Integration test to run the application.
 *
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
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

		Table1 t1 = createTable1Instance();

		em.persist(t1);
		em.flush();
	}

	private Table1 createTable1Instance() {
		Table2 t2 = new Table2();
		t2.id1 = "21";
		t2.id2 = "22";

		Table1 t1 = new Table1();
		t1.id = "21";
		t1.table2 = t2;
		return t1;
	}

	@Test
	public void accessIdViaMetadataTable1() {

		Metamodel metamodel = em.getMetamodel();

		ManagedType<Table1> managedType = metamodel.managedType(Table1.class);

		assertTrue(managedType instanceof IdentifiableType);

		IdentifiableType identifiableType = (IdentifiableType) managedType;

		assertFalse(identifiableType.hasSingleIdAttribute());
	}

	@Test
	public void accessIdViaMetadataTable2() {

		Metamodel metamodel = em.getMetamodel();

		ManagedType<Table2> managedType = metamodel.managedType(Table2.class);

		assertTrue(managedType instanceof IdentifiableType);

		IdentifiableType identifiableType = (IdentifiableType) managedType;

		System.out.println(identifiableType);

		assertFalse(identifiableType.hasSingleIdAttribute());
	}


	@Test
	public void testSD() {

		JpaEntityInformation<Table1, ?> information = getEntityInformation(
				Table1.class, em);

		Table1 t1 = createTable1Instance();

		Object id = information.getId(t1);

		assertNotNull(id);
	}
}
