package app.k8ty.models

import app.k8ty.crypto.Crypto
import app.k8ty.melvin.doobie.io.{ Account, Organization }
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class AccountIOIntegrationSpec extends AnyFlatSpec with should.Matchers with BeforeAndAfter {

  val orgId: String  = "AccountIOIntegrationSpec1"
  val orgId2: String = "AccountIOIntegrationSpec2"
  val orgId3: String = "AccountIOIntegrationSpec3"

  before {
    Organization.createOrganization(orgId).unsafeRunSync()
    Organization.createOrganization(orgId2).unsafeRunSync()
    Organization.createOrganization(orgId3).unsafeRunSync()
  }

  after {
    Organization.deleteOrganization(orgId).unsafeRunSync()
    Organization.deleteOrganization(orgId2).unsafeRunSync()
    Organization.deleteOrganization(orgId3).unsafeRunSync()
  }

  "Account IO" should "properly register new accounts" in {
    val acc1: Option[Account] = Account.registerAccount(Crypto.generatePublicKey, None, None).unsafeRunSync()
    assert(acc1.isDefined)
    assert(acc1.exists(_.hashedPassword.isEmpty))
    assert(acc1.exists(_.organizations.isEmpty))

    val acc2: Option[Account] = Account.registerAccount(Crypto.generatePublicKey, Option(Crypto.generatePrivateKey), None).unsafeRunSync()
    assert(acc2.isDefined)
    assert(acc2.exists(_.hashedPassword.isDefined))
    assert(acc2.exists(_.organizations.isEmpty))

    val acc3: Option[Account] = Account.registerAccount(Crypto.generatePublicKey, None, Option(orgId)).unsafeRunSync()
    assert(acc3.isDefined)
    assert(acc3.exists(_.hashedPassword.isEmpty))
    assert(acc3.exists(_.organizations.nonEmpty))

  }

  it should "not allow registration of an account with an ID that already exits" in {
    val uniqueId: String = Crypto.generatePublicKey
    assert(Account.registerAccount(uniqueId).unsafeRunSync().isDefined)
    assert(Account.registerAccount(uniqueId).unsafeRunSync().isEmpty)
  }

  it should "allow registration, but unset an organization that doesn't exist" in {
    val account: Option[Account] = Account.registerAccount(Crypto.generatePublicKey, None, Option(Crypto.generatePublicKey)).unsafeRunSync()
    assert(account.isDefined)
    assert(account.exists(_.organizations.isEmpty))
  }

  it should "be able to get an account by ID" in {
    val id: String              = Crypto.generatePublicKey
    val acct: Option[Account]   = Account.registerAccount(id).unsafeRunSync()
    assert(acct.nonEmpty)
    val lookup: Option[Account] = Account.getAccountById(id).unsafeRunSync()
    assert(lookup.nonEmpty)
    assert(acct.equals(lookup))
  }

  it should "be able to add an organization to an account" in {
    val id: String = Crypto.generatePublicKey
    val acc        = Account.registerAccount(id).unsafeRunSync()
    assert(acc.exists(_.organizations.isEmpty))
    val update     = Account.addOrganizationToAccount(id, orgId).unsafeRunSync()
    assert(update == 1)
    assert(Account.getAccountById(id).unsafeRunSync().exists(_.organizations.nonEmpty))
  }

  it should "be not able to add an organization that doesn't exist to an account" in {
    val id: String = Crypto.generatePublicKey
    val acc        = Account.registerAccount(id).unsafeRunSync()
    assert(acc.exists(_.organizations.isEmpty))
    val update     = Account.addOrganizationToAccount(id, Crypto.generatePublicKey).unsafeRunSync()
    assert(update == 0)
    assert(Account.getAccountById(id).unsafeRunSync().exists(_.organizations.isEmpty))
  }

  it should "be able to remove an organization from an account" in {
    val id: String = Crypto.generatePublicKey
    val acc        = Account.registerAccount(id, None, Option(orgId)).unsafeRunSync()
    assert(acc.exists(_.organizations.nonEmpty))
    val update     = Account.removeOrganizationFromAccount(id, orgId).unsafeRunSync()
    assert(update == 1)
    assert(Account.getAccountById(id).unsafeRunSync().exists(_.organizations.isEmpty))
  }

  it should "be able to remove all organizations from an account" in {
    val id: String = Crypto.generatePublicKey
    val acc        = Account.registerAccount(id, None, Option(orgId)).unsafeRunSync()
    assert(acc.exists(_.organizations.nonEmpty))
    assert(
      Account.addOrganizationToAccount(id, orgId2).unsafeRunSync() == 1
    )
    assert(
      Account.addOrganizationToAccount(id, orgId3).unsafeRunSync() == 1
    )
    assert(Account.getAccountById(id).unsafeRunSync().exists(_.organizations.length == 3))
    assert(
      Account.removeAllOrganizationFromAccount(id).unsafeRunSync() == 1
    )
    assert(Account.getAccountById(id).unsafeRunSync().exists(_.organizations.isEmpty))
  }

  it should "be able to validate a password" in {
    val id: String = Crypto.generatePublicKey
    val pw: String = Crypto.generatePrivateKey
    assert(Account.registerAccount(id, Option(pw), None).unsafeRunSync().nonEmpty)
    assert(Account.validatePassword(id, pw).unsafeRunSync())
    assert(!Account.validatePassword("fake id", pw).unsafeRunSync())
    assert(!Account.validatePassword(id, "bad password").unsafeRunSync())
  }

  it should "be able to update a password" in {
    val id: String  = Crypto.generatePublicKey
    val pw: String  = Crypto.generatePrivateKey
    val pw2: String = Crypto.generatePrivateKey
    assert(Account.registerAccount(id, Option(pw), None).unsafeRunSync().nonEmpty)
    assert(Account.validatePassword(id, pw).unsafeRunSync())
    assert(Account.updateAccountPassword(id, pw, pw2).unsafeRunSync() == 1)
    assert(Account.updateAccountPassword(id, pw, pw2).unsafeRunSync() == 0)
    assert(Account.validatePassword(id, pw2).unsafeRunSync())
  }

  it should "be able to reset a password" in {
    val id: String  = Crypto.generatePublicKey
    val pw: String  = Crypto.generatePrivateKey
    val pw2: String = Crypto.generatePrivateKey
    assert(Account.registerAccount(id, Option(pw), None).unsafeRunSync().nonEmpty)
    assert(Account.validatePassword(id, pw).unsafeRunSync())
    assert(Account.resetAccountPassword(id, pw2).unsafeRunSync() == 1)
    assert(!Account.validatePassword(id, pw).unsafeRunSync())
    assert(Account.validatePassword(id, pw2).unsafeRunSync())
  }

  it should "be able to deactivate an account" in {
    val id: String = Crypto.generatePublicKey
    val pw: String = Crypto.generatePrivateKey
    assert(Account.registerAccount(id, Option(pw), None).unsafeRunSync().nonEmpty)
    assert(Account.validatePassword(id, pw).unsafeRunSync())
    assert(Account.deactivateAccount(id).unsafeRunSync() == 1)
    assert(!Account.validatePassword(id, pw).unsafeRunSync())
    assert(Account.getAccountById(id).unsafeRunSync().exists(_.hashedPassword.isEmpty))
  }

  it should "be able to delete an account" in {
    val id: String = Crypto.generatePublicKey
    assert(Account.registerAccount(id).unsafeRunSync().nonEmpty)
    assert(Account.deleteAccount(id).unsafeRunSync() == 1)
    assert(Account.getAccountById(id).unsafeRunSync().isEmpty)
  }

}
