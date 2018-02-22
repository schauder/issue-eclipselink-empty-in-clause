package sample.data.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan
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

		Table1 t1 = new Table1();
		t1.id = "21";
		t1.id2 = "22";
		return t1;
	}

	private Table2 createTable2() {
		Table2 t2 = new Table2();
		t2.id1 = "21";
		t2.id2 = "22";
		return t2;
	}

	@Test
	public void accessIdViaMetadataTable1() {

		Metamodel metamodel = em.getMetamodel();

		ManagedType<Table1> managedType = metamodel.managedType(Table1.class);

		assertTrue(managedType instanceof IdentifiableType);

		IdentifiableType identifiableType = (IdentifiableType) managedType;

		assertFalse(identifiableType.hasSingleIdAttribute());
	}

	/**
	 * This one fails, although it shouldn't.
	 * Difference to {@link #accessIdViaMetadataTable1()} is that it uses {@link Table2} which has an inner class as the idclass.
	 */
	@Test
	public void accessIdViaMetadataTable2() {

		Metamodel metamodel = em.getMetamodel();

		ManagedType<Table2> managedType = metamodel.managedType(Table2.class);

		assertTrue(managedType instanceof IdentifiableType);

		IdentifiableType identifiableType = (IdentifiableType) managedType;

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

	@Test
	// this fails with an exception because the return values from EclipseLink are inconsistent.
	public void testSD2() {

		JpaEntityInformation<Table2, ?> information = getEntityInformation(
				Table2.class, em);

		Table2 t2 = createTable2();

		Object id = information.getId(t2);

		assertNotNull(id);
	}
}
