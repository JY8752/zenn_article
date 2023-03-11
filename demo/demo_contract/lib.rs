// 標準ライブラリがなかったら標準ライブラリを使わない宣言
#![cfg_attr(not(feature = "std"), no_std)]

// Contract定義のエントリーポイント
#[ink::contract]
mod erc721 {
    //use ink::prelude::string::{String, ToString};
    use ink::storage::Mapping; // inkからMapping structをimport.スマートコントラクト用に用意されているのでMapにはこれを使う。
    use scale::{Decode, Encode};

    pub type TokenId = u32; // TokenId

    // ストレージ定義
    #[ink(storage)]
    #[derive(Default)] // Default traitを実装
    pub struct Erc721 {
        token_owner: Mapping<TokenId, AccountId>,
        token_approvals: Mapping<TokenId, AccountId>,
        owned_tokens_count: Mapping<AccountId, u32>,
        operator_approvals: Mapping<(AccountId, AccountId), ()>,
    }

    // エラー定義
    #[derive(Encode, Decode, Debug, PartialEq, Eq, Clone, Copy)] // いろいろtraitを実装
    #[cfg_attr(feature = "std", derive(scale_info::TypeInfo))]
    pub enum Error {
        NotOwner,
        NotApproved,
        TokenExists,
        TokenNotFound,
        CannotInsert,
        CannotFetchValue,
        NotAllowed,
    }
}
