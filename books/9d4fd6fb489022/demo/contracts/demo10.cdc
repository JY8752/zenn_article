// Valid
pub contract FungibleToken {

    pub event Deposit(balance: Int)

    pub resource interface Receiver {

        pub balance: Int

        pub fun deposit(from: @{Receiver}) {
            pre {
                from.balance > 0:
                    "Deposit balance needs to be positive!"
            }
            post {
                self.balance == before(self.balance) + before(from.balance):
                    "Incorrect amount removed"
            }
        }
    }

    pub resource Vault: Receiver {

        // keeps track of the total balance of the accounts tokens
        pub var balance: Int

        init(balance: Int) {
            self.balance = balance
        }

        // withdraw subtracts amount from the vaults balance and
        // returns a vault object with the subtracted balance
        pub fun withdraw(amount: Int): @Vault {
            self.balance = self.balance - amount
            return <-create Vault(balance: amount)
        }

        // deposit takes a vault object as a parameter and adds
        // its balance to the balance of the Account's vault, then
        // destroys the sent vault because its balance has been consumed
        pub fun deposit(from: @{Receiver}) {
            self.balance = self.balance + from.balance
            destroy from
            emit Deposit(balance: 1)
        }
    }

    pub fun createVault(initialBalance: Int): @Vault {
        return <-create Vault(balance: initialBalance)
    }
}

// pub fun main() {
//   let vault <- FungibleToken.createVault(initialBalance: 1)
//   destroy vault
// }