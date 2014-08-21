package org.hibernate.test.naturalid.cid;
public class Account {
    private AccountId accountId;
    private String shortCode;
    
    protected Account() {
    }
    
    public Account(AccountId accountId, String shortCode) {
        this.accountId = accountId;
        this.shortCode = shortCode;
    }
    public String getShortCode() {
        return shortCode;
    }
    public AccountId getAccountId() {
        return accountId;
    }
}
