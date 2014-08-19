package org.hibernate.test.annotations.naturalid.cid;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import org.hibernate.annotations.NaturalId;

@Entity
public class A {
    @EmbeddedId
    private AId accountId;
    @NaturalId(mutable = false)
    private String shortCode;
    
    protected A() {
    }
    
    public A(AId accountId, String shortCode) {
        this.accountId = accountId;
        this.shortCode = shortCode;
    }
    public String getShortCode() {
        return shortCode;
    }
    public AId getAccountId() {
        return accountId;
    }
}
