package com.devhacks.voting;

import com.chain.api.*;
import com.chain.exception.ChainException;
import com.chain.http.Client;
import com.chain.signing.HsmSigner;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RestController
public class AccountController {

    private Client client = new Client();


    @RequestMapping(path = "/create-voting-account", method = RequestMethod.POST)
    public VotingAccount createVotingAccount() throws ChainException, IOException {

        String accountAlias = UUID.randomUUID().toString().substring(0, 20);

        return createVotingAccountWithAlias(accountAlias);
    }

    @RequestMapping(path = "/create-voting-account-alias", method = RequestMethod.POST)
    public VotingAccount createVotingAccountWithAlias(@RequestParam("alias") String accountAlias) throws ChainException, IOException {
        MockHsm.Key key = initializeKey();

        initVoteAsset(key);

        Account account = new Account.Builder()
                .setAlias(accountAlias)
                .addRootXpub(key.xpub)
                .setQuorum(1)
                .create(client);


        // issue 1 vote credit
        Transaction.Template issuance = new Transaction.Builder()
                .addAction(new Transaction.Action.Issue()
                                .setAssetAlias("vote")
                                .setAmount(1)
                ).addAction(new Transaction.Action.ControlWithAccount()
                                .setAccountAlias(account.alias)
                                .setAssetAlias("vote")
                                .setAmount(1)
                ).build(client);


        FileOutputStream outputStream = new FileOutputStream(new File(accountAlias+".png"));
        QRCode.from(accountAlias).withSize(500,500).to(ImageType.PNG).writeTo(outputStream);
        outputStream.close();

        Transaction.submit(client, HsmSigner.sign(issuance));

        return new VotingAccount(account.alias, account.id);
    }

    @RequestMapping(path = "/vote", method = RequestMethod.POST)
    public void vote(@RequestParam("voter") String voter,
                     @RequestParam("candidate") String candidate) throws ChainException {

        initializeKey();

        Transaction.Template payment = new Transaction.Builder()
                .addAction(new Transaction.Action.SpendFromAccount()
                                .setAccountAlias(voter)
                                .setAssetAlias("vote")
                                .setAmount(1)
                ).addAction(new Transaction.Action.ControlWithAccount()
                                .setAccountAlias(candidate)
                                .setAssetAlias("vote")
                                .setAmount(1)
                ).build(client);

        Transaction.Template signedPayment = HsmSigner.sign(payment);

        Transaction.submit(client, signedPayment);

    }


    private MockHsm.Key initializeKey() throws ChainException {
        MockHsm.Key.Items rootKeys = new MockHsm.Key.QueryBuilder().addAlias("rootKey").execute(client);
        if (rootKeys.hasNext()) {
            MockHsm.Key next = rootKeys.next();
            HsmSigner.addKey(next, MockHsm.getSignerClient(client));
            return next;
        } else {
            MockHsm.Key key = MockHsm.Key.create(client, "rootKey");
            HsmSigner.addKey(key, MockHsm.getSignerClient(client));
            return key;
        }
    }

    @RequestMapping(path = "/create-candidate-account", method = RequestMethod.POST)
    public CandidateAccount createCandidateAccount(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("party") String party,
            @RequestParam("imageUrl") String imageUrl
    )
            throws ChainException {
        MockHsm.Key key = initializeKey();
        String candidateAlias = "candidate-account-" + firstName + "-" + lastName;

        Account account = new Account.Builder()
                .setAlias(candidateAlias)
                .addRootXpub(key.xpub)
                .setQuorum(1)
                .addTag("firstName", firstName)
                .addTag("lastName", lastName)
                .addTag("party", party)
                .addTag("imageUrl", imageUrl)
                .addTag("candidate", "yes")
                .create(client);

        return new CandidateAccount(account);
    }


    @RequestMapping(path = "/candidate-statistics", method = RequestMethod.GET)
    public List<CandidateAccount> getCandidates() throws ChainException {

        List<CandidateAccount> candidateAccounts = new ArrayList<>();

        Account.Items candidateChainAccounts = new Account.QueryBuilder()
                .setFilter("tags.candidate=$1")
                .addFilterParameter("yes")
                .execute(client);

        while (candidateChainAccounts.hasNext()) {
            Account account = candidateChainAccounts.next();
            CandidateAccount acc = new CandidateAccount(account);
            candidateAccounts.add(acc);
            Balance.Items candidateBalance = new Balance.QueryBuilder()
                    .setFilter("account_alias=$1")
                    .addFilterParameter(account.alias)
                    .execute(client);

            while (candidateBalance.hasNext()) {
                Balance next = candidateBalance.next();
                acc.setBalance(next.amount);
            }
        }

        return candidateAccounts;
    }

    private void initVoteAsset(MockHsm.Key key) throws ChainException {
        Asset.Items items = new Asset.QueryBuilder().setFilter("alias='vote'").execute(client);

        if (items.hasNext()) {
            return;
        }

        new Asset.Builder()
                .setAlias("vote")
                .addRootXpub(key.xpub)
                .setQuorum(1)
                .create(client);
    }

}
