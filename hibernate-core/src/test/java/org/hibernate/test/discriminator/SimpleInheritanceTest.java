/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2006-2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.test.discriminator;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.mapping.MetadataSource;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Gavin King
 */
public class SimpleInheritanceTest extends BaseCoreFunctionalTestCase {
	@Override
	public String[] getMappings() {
		return new String[] { "discriminator/SimpleInheritance.hbm.xml" };
	}

	@Test
	public void testDiscriminatorSubclass() {
		Session s = openSession();
		Transaction t = s.beginTransaction();

		Employee mark = new Employee();
		mark.setId( 1 );
		mark.setName( "Mark" );
		mark.setTitle( "internal sales" );
		mark.setSex( 'M' );

		Customer joe = new Customer();
		joe.setId( 2 );
		joe.setName( "Joe" );
		joe.setComments( "Very demanding" );
		joe.setSex( 'M' );

		Person yomomma = new Person();
		yomomma.setId( 3 );
		yomomma.setName("mum");
		yomomma.setSex('F');

		s.save(yomomma);
		s.save(mark);
		s.save(joe);

		assertEquals( s.createQuery("from java.io.Serializable").list().size(), 0 );

		assertEquals( s.createQuery("from org.hibernate.test.discriminator.Person").list().size(), 3 );
		assertEquals( s.createQuery("from org.hibernate.test.discriminator.Person p where p.class = org.hibernate.test.discriminator.Person").list().size(), 1 );
		assertEquals( s.createQuery("from org.hibernate.test.discriminator.Person p where p.class = org.hibernate.test.discriminator.Customer").list().size(), 1 );
		assertEquals( s.createQuery("from org.hibernate.test.discriminator.Person p where type(p) = :who").setParameter("who", Person.class).list().size(), 1 );
		assertEquals( s.createQuery("from org.hibernate.test.discriminator.Person p where type(p) in :who").setParameterList("who", new Class[] {Customer.class, Person.class}).list().size(), 2 );
		s.clear();

		List customers = s.createQuery("from org.hibernate.test.discriminator.Customer").list();
		for ( Object customer : customers ) {
			Customer c = (Customer) customer;
			assertEquals( "Very demanding", c.getComments() );
		}
		assertEquals( customers.size(), 1 );
		s.clear();

		mark = (Employee) s.get( Employee.class, mark.getId() );
		joe = (Customer) s.get( Customer.class, joe.getId() );

 		s.delete(mark);
		s.delete(joe);
		s.delete(yomomma);
		assertTrue( s.createQuery("from org.hibernate.test.discriminator.Person").list().isEmpty() );
		t.commit();
		s.close();
	}

	@Test
	public void testAccessAsIncorrectSubclass() {
		Session s = openSession();
		s.beginTransaction();
		Employee e = new Employee();
		e.setId( 4 );
		e.setName( "Steve" );
		e.setSex( 'M' );
		e.setTitle( "grand poobah" );
		s.save( e );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		Customer c = ( Customer ) s.get( Customer.class, e.getId() );
		s.getTransaction().commit();
		s.close();
		assertNull( c );

		s = openSession();
		s.beginTransaction();
		e = ( Employee ) s.get( Employee.class, e.getId() );
		c = ( Customer ) s.get( Customer.class, e.getId() );
		s.getTransaction().commit();
		s.close();
		assertNotNull( e );
		assertNull( c );

		s = openSession();
		s.beginTransaction();
		s.delete( e );
		s.getTransaction().commit();
		s.close();
	}

	@Test
	public void testQuerySubclassAttribute() {
		Session s = openSession();
		Transaction t = s.beginTransaction();
		Person p = new Person();
		p.setId( 5 );
		p.setName("Emmanuel");
		p.setSex('M');
		s.save( p );
		Employee q = new Employee();
		q.setId( 6 );
		q.setName("Steve");
		q.setSex('M');
		q.setTitle("Mr");
		q.setSalary( new BigDecimal(1000) );
		s.save( q );

		List result = s.createQuery("from org.hibernate.test.discriminator.Person where salary > 100").list();
		assertEquals( result.size(), 1 );
		assertSame( result.get(0), q );

		result = s.createQuery("from org.hibernate.test.discriminator.Person where salary > 100 or name like 'E%'").list();
		assertEquals( result.size(), 2 );

		result = s.createCriteria(Person.class)
			.add( Property.forName("salary").gt( new BigDecimal(100) ) )
			.list();
		assertEquals( result.size(), 1 );
		assertSame( result.get(0), q );

		//TODO: make this work:
		/*result = s.createQuery("select salary from Person where salary > 100").list();
		assertEquals( result.size(), 1 );
		assertEquals( result.get(0), new BigDecimal(1000) );*/

		s.delete(p);
		s.delete(q);
		t.commit();
		s.close();
	}

	@Test
	public void testLoadSuperclassProxyPolymorphicAccess() {
		Session s = openSession();
		s.beginTransaction();
		Employee e = new Employee();
		e.setId( 7 );
		e.setName( "Steve" );
		e.setSex( 'M' );
		e.setTitle( "grand poobah" );
		s.save( e );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		// load the superclass proxy.
		Person pLoad = ( Person ) s.load( Person.class, new Long( e.getId() ) );
		assertTrue( pLoad instanceof HibernateProxy);
		Person pGet = ( Person ) s.get( Person.class, e.getId());
		Person pQuery = ( Person ) s.createQuery( "from org.hibernate.test.discriminator.Person where id = :id" )
				.setLong( "id", e.getId() )
				.uniqueResult();
		Person pCriteria = ( Person ) s.createCriteria( Person.class )
				.add( Restrictions.idEq( e.getId() ) )
				.uniqueResult();
		// assert that executing the queries polymorphically returns the same proxy
		assertSame( pLoad, pGet );
		assertSame( pLoad, pQuery );
		assertSame( pLoad, pCriteria );

		// assert that the proxy is not an instance of Employee
		assertFalse( pLoad instanceof Employee );

		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		s.delete( e );
		s.getTransaction().commit();
		s.close();
	}

	@Test
	public void testLoadSuperclassProxyEvictPolymorphicAccess() {
		Session s = openSession();
		s.beginTransaction();
		Employee e = new Employee();
		e.setId( 8 );
		e.setName( "Steve" );
		e.setSex( 'M' );
		e.setTitle( "grand poobah" );
		s.save( e );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		// load the superclass proxy.
		Person pLoad = ( Person ) s.load( Person.class, new Long( e.getId() ) );
		assertTrue( pLoad instanceof HibernateProxy);
		// evict the proxy
		s.evict( pLoad );
		Employee pGet = ( Employee ) s.get( Person.class, e.getId() );
		Employee pQuery = ( Employee ) s.createQuery( "from org.hibernate.test.discriminator.Person where id = :id" )
				.setLong( "id", e.getId() )
				.uniqueResult();
		Employee pCriteria = ( Employee ) s.createCriteria( Person.class )
				.add( Restrictions.idEq( e.getId() ) )
				.uniqueResult();
		// assert that executing the queries polymorphically returns the same Employee instance
		assertSame( pGet, pQuery );
		assertSame( pGet, pCriteria );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		s.delete( e );
		s.getTransaction().commit();
		s.close();
	}
}
