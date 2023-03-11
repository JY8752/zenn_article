pub contract TestContract {
    pub let storagePath: StoragePath
    pub let publicPath: PublicPath
    pub let publicHelloPath: PublicPath
    pub let privatePath: PrivatePath

    pub var id: UInt64

    pub resource interface Hello {
        pub fun hello() {
            log("hello")
        }
    }

    pub resource Token: Hello {
        pub let id: UInt64

        init(id: UInt64) {
            self.id = id
        }

        pub fun getTokenId(): UInt64 {
            return self.id
        } 
    }

    pub fun mint(): @Token {
        let token <- create Token(id: self.id)
        self.id = self.id + 1
        return <-token
    }

    init() {
        self.storagePath = /storage/test
        self.publicPath = /public/test
        self.publicHelloPath = /public/hello
        self.privatePath = /private/test

        self.id = 1

        // リソースをストレージに保存
        self.account.save(<- self.mint(), to: self.storagePath)
        
        // Capabilityを作成する
        
        // リソースのCapabilityを作成する
        self.account.link<&Token>(self.publicPath, target: self.storagePath)
        
        // リソースの機能をHelloに限定してCapabilityを作成
        self.account.link<&{Hello}>(self.publicHelloPath, target: self.storagePath)

        // privateパスにCapabilityを作成する
        self.account.link<&Token>(self.privatePath, target: self.storagePath)
    }
}