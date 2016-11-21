package com.devhacks.voting;

import com.chain.api.Account;

/**
 * Created by 286868 on 11/19/2016.
 */
public class CandidateAccount {

    private String party;

    private String firstName;

    private String lastName;

    private String imageUrl;

    private String alias;

    private String acctId;

    private long balance ;


    public CandidateAccount(Account account) {
        this.acctId= account.id;
        this.alias = account.alias;
        this.firstName = getString(account,"firstName") ;
        this.lastName = getString(account, "lastName");
        this.imageUrl = getString(account, "imageUrl");
        this.party = getString(account, "party");
    }

    private String getString(Account account, String name) {
        return account.tags.get(name) != null ? account.tags.get(name).toString() : null;
    }


    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }
}
