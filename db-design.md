# やること管理アプリ DB設計書

## 1. 目的と設計範囲

本書は、`handout_app_spec.md` と `requirements.md` に基づき、アプリで使用するデータベースの構造を定めます。画面の配置や見た目は、別途作成する画面設計書で扱います。

データベースは MySQL とし、使用するテーブルは `todos` の1つだけです。1件のやることをテーブルの1行に保存します。

## 2. テーブル一覧

| テーブル名 | 用途 |
|---|---|
| `todos` | やること、ジャンル、優先度、期限、メモ、完了状態と登録・更新日時を保存する |

## 3. `todos` テーブル定義

| カラム名 | 型 | NULL | 既定値・自動設定 | 内容・制約 |
|---|---|---|---|---|
| `id` | `BIGINT` | 不可 | 自動採番 | 1件を識別する番号。主キー（行を一意に識別する列） |
| `title` | `VARCHAR(255)` | 不可 | なし | やること。空白だけの値はアプリ側で受け付けない |
| `detail` | `VARCHAR(255)` | 可 | `NULL` | メモ |
| `category` | `VARCHAR(255)` | 不可 | なし | ジャンル。デザイン、マーケティング、プログラミング、資格、就職活動のいずれか |
| `priority` | `INT` | 不可 | `2` | 優先度。`1`＝高、`2`＝中、`3`＝低 |
| `due_date` | `DATE` | 可 | `NULL` | 期限 |
| `completed` | `BOOLEAN` | 不可 | `FALSE` | 完了状態。`FALSE`＝未完了、`TRUE`＝完了 |
| `created_at` | `DATETIME` | 不可 | 登録時に自動設定 | 登録日時 |
| `updated_at` | `DATETIME` | 不可 | 登録時と更新時に自動設定 | 最終更新日時 |

### 3.1 値の扱い

- `category` は仕様で定められた5種類からアプリが選択・保存します。
- `priority` は数値で保存し、画面では「高」「中」「低」と表示します。
- `completed` は真偽値（はい／いいえを表す値）で保存し、画面では「完了」「未完了」と表示します。
- `id`、`created_at`、`updated_at` は利用者の入力項目にせず、データベースが値を設定します。
- `title` と `detail` の内容は操作記録に含めません。操作記録には操作の種類と対象の `id` のみを残します。

## 4. 要件とカラムの対応

| 要件 | 対応カラム | 説明 |
|---|---|---|
| やることの登録、一覧表示、名前での絞り込み | `title` | やることの名前を保存し、一部一致検索に使う |
| メモの登録・編集 | `detail` | 任意のメモを保存する |
| ジャンルの選択・完全一致絞り込み | `category` | 仕様で定められたジャンルを保存し、完全一致で絞り込む |
| 優先度の登録・表示 | `priority` | `1`／`2`／`3` を保存し、それぞれ高／中／低として表示する |
| 期限の登録・表示・並び替え | `due_date` | 任意の期限を保存し、近い順または遠い順に並べ替える |
| 完了状態の編集・表示 | `completed` | 完了か未完了かを保存する。初期値は未完了 |
| 1件の識別、編集・削除の対象指定、操作記録 | `id` | 自動採番した番号を使って対象の行を識別する |
| 登録日時の自動設定 | `created_at` | 登録時にデータベースが設定する |
| 更新日時の自動設定 | `updated_at` | 登録時および更新時にデータベースが設定する |

## 5. DDL（テーブル作成SQL）

次のSQLは `todos` テーブルを作成します。`AUTO_INCREMENT` は番号を自動で増やす指定、`DEFAULT` は値を省略したときの初期値です。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_priority CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_todos_category CHECK (
        category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

`CHECK` は列に保存できる値を制限する指定です。優先度とジャンルが定められた選択肢に限られるようにしています。`utf8mb4` は日本語を含む文字を保存するための文字コードです。

## 6. 設計上の補足

- `detail` と `due_date` は任意項目のため、値がない場合は `NULL`（未設定を表す値）を保存します。
- `title` の空文字・空白だけの入力や、255文字を超える入力の拒否は、仕様に記載された入力チェックとしてアプリ側で行います。
- 操作記録の保存先や形式は叩き台で定められていません。テーブルを `todos` の1つに限定する前提に従い、別のログテーブルは設けません。アプリのログには操作種別と `id` のみを記録し、タイトルとメモは含めません。
- DDLには、仕様で定められた優先度とジャンルの選択肢を保つための `CHECK` 制約を含めています。
