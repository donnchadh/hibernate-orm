package org.hibernate.test.naturalid.cid;

import static org.junit.Assert.assertNotNull;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.criterion.Restrictions;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

public class CompositeIdAndNaturalIdTest extends BaseCoreFunctionalTestCase {

    public String[] getMappings() {
        return new String[] { "naturalid/cid/Account.hbm.xml" };
    }

    public void configure(Configuration cfg) {
        cfg.setProperty( Environment.USE_SECOND_LEVEL_CACHE, "false" );
        cfg.setProperty( Environment.USE_QUERY_CACHE, "false" );
        cfg.setProperty( Environment.GENERATE_STATISTICS, "false" );
    }

    @TestForIssue(jiraKey = "HHH-9333")
    @Test
    public void testSave() {
        // prepare some test data...
        Session session = openSession();
        session.beginTransaction();
        Account account = new Account(new AccountId(1), "testAcct");
        session.save( account );
        session.getTransaction().commit();
        session.close();

        // clean up
        session = openSession();
        session.beginTransaction();
        session.delete( account );
        session.getTransaction().commit();
        session.close();
    }

    @TestForIssue(jiraKey = "HHH-9333")
    @Test
    public void testNaturalIdCriteria() {
        Session s = openSession();
        s.beginTransaction();
        Account u = new Account(new AccountId(1), "testAcct" );
        s.persist( u );
        s.getTransaction().commit();
        s.close();

        s = openSession();
        s.beginTransaction();
        u = ( Account ) s.createCriteria( Account.class )
                .add( Restrictions.naturalId().set( "shortCode", "testAcct" ) )
                .setCacheable( true )
                .uniqueResult();
        assertNotNull( u );
        s.getTransaction().commit();
        s.close();

        s = openSession();
        s.beginTransaction();
        s.createQuery( "delete Account" ).executeUpdate();
        s.getTransaction().commit();
        s.close();
    }

    @TestForIssue(jiraKey = "HHH-9333")
    @Test
    public void testByNaturalId() {
        Session s = openSession();
        s.beginTransaction();
        Account u = new Account(new AccountId(1), "testAcct" );
        s.persist( u );
        s.getTransaction().commit();
        s.close();

        s = openSession();
        s.beginTransaction();
        u = ( Account ) s.byNaturalId(Account.class).using("shortCode", "testAcct").load();
        assertNotNull( u );
        s.getTransaction().commit();
        s.close();

        s = openSession();
        s.beginTransaction();
        s.createQuery( "delete Account" ).executeUpdate();
        s.getTransaction().commit();
        s.close();
    }

}
