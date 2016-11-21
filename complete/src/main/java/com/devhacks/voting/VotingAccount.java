package com.devhacks.voting;

/**
 * Created by 286868 on 11/19/2016.
 */
public class VotingAccount {

    private String acctId;

    private String alias;

    public VotingAccount(String alias, String s) {
        this.alias = alias;
        this.acctId = s;
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
