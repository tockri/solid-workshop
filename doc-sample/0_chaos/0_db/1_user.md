# ユーザーテーブル

## テーブル名

user

## カラム定義

| name       | Key | type         | null |                |
| ---------- | --- | ------------ | ---- | -------------- |
| id         | PK  | bigint       | no   | auto_increment |
| account_id | FK  | varchar(20)  | no   |                |
| last_login |     | datetime     | yes  |                |
| score      |     | int          | no   | default 0      |
| password   |     | varchar(128) | no   |                |

※ これはサンプルのため、ログイン機能やセキュリティは省略している。パスワードはDBに平文で登録されている。
