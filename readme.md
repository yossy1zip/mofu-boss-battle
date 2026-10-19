# MofuBossBattle

Minecraft サーバー **MOFUCRAFT!!!** で動かしている、1 人用ボス戦の管理プラグイン(Java 21・Paper 1.21・Gradle)です。

> これは実運用のプラグインから、サーバーの設定・秘密を抜いた**公開用の写し**です(履歴は 1 コミット)。
> `./gradlew build` でビルドと単体テストが通ります(テスト 7 件。`TimeUtil` の時間の整形・解釈)。CI の見本は `docs/ci.example.yml`(GitHub Actions)。

## 作った理由 / 自分がしたこと / AI に任せたこと
<!-- 下書き: 内容は本人が確認して直す(コードと過去の発言から起こした) -->

- **なぜ**: ボス戦を「1 人ずつ・制限時間つき・タイムを順位に残す」形にしたかった。プレイヤーが待機部屋で、今ボスが空いているかを見られるようにした。
- **自分が決めたこと**: 仕様(1 人用・制限時間・撃破後のアイテム回収フェーズ・勝敗でコマンドを実行)/ ランキングを MySQL に残して PlaceholderAPI の看板で見せる設計 / 本番サーバーへの導入と運用。
- **AI に任せたこと**: 実装の大部分(AI コーディングエージェントに指示して書かせ、サーバーで動かして確認)。単体テストは公開用の写しを作るときに追加した。
- 分かっている弱い点: 戦闘の状態管理(`BattleManager`・`BattleSession`)はテストがなく、サーバー上で確かめている。`TimeUtil.parseTimeString` は単位の無い末尾の数字を無視する(テストで挙動を固定)。

## 機能

- **ボス戦管理**: コマンドでボス戦を開始、1人のみ参加可能
- **時間制限**: ボスごとに可変の制限時間を設定
- **カスタムメッセージ**: 開始・勝利・敗北・残り時間メッセージをボスごとに設定
- **ランキング**: 討伐時間をMySQLに記録、PlaceholderAPIで表示可能
- **MythicMobs連携**: ボス撃破イベントを自動検知
- **アイテム回収フェーズ**: 撃破後にアイテム回収時間を設定可能

## 依存プラグイン

- **必須**: MythicMobs
- **任意**: PlaceholderAPI

## インストール

1. `MofuBossBattle-x.x.x.jar` を `plugins/` フォルダに配置
2. サーバーを起動して設定ファイルを生成
3. `plugins/MofuBossBattle/config.yml` でMySQL接続情報を設定
4. `plugins/MofuBossBattle/bosses/` にボス設定ファイルを作成
5. `/boss reload` で設定を再読み込み

## コマンド

| コマンド | 権限 | 説明 |
|---------|------|------|
| `/boss start <boss_id> [player]` | `mofubossbattle.start` | ボス戦を開始 |
| `/boss stop [player]` | `mofubossbattle.stop` | ボス戦を強制終了 |
| `/boss list` | `mofubossbattle.list` | 利用可能なボス一覧 |
| `/boss status [boss_id]` | `mofubossbattle.status` | ボスが現在挑戦中かどうかを確認（待機部屋でバフアイテムを使う前の確認用） |
| `/boss ranking <boss_id>` | `mofubossbattle.ranking` | ランキング表示 |
| `/boss myrank <boss_id>` | `mofubossbattle.myrank` | 自分の順位を確認 |
| `/boss reload` | `mofubossbattle.reload` | 設定を再読み込み |
| `/boss help` | - | ヘルプ表示 |

## 設定ファイル

### config.yml

```yaml
database:
  host: "localhost"
  port: 3306
  database: "mofubossbattle"
  username: "root"
  password: "change-me"
  pool-size: 10

settings:
  prefix: "&8[&6MofuBossBattle&8] "
  default-time-limit: 300
  default-item-collection-time: 30
  enabled: true

debug: false
```

### ボス設定 (bosses/example_boss.yml)

```yaml
id: "example_boss"
display-name: "&c&lドラゴンキング"
mythic-mob-id: "DragonKing"
time-limit: 600
item-collection-time: 45

locations:
  teleport:
    world: "world"
    x: 100.5
    y: 65.0
    z: 200.5
    yaw: 0.0
    pitch: 0.0
  boss-spawn:
    world: "world"
    x: 100.5
    y: 65.0
    z: 220.5
  exit:
    world: "world"
    x: 0.5
    y: 100.0
    z: 0.5

messages:
  start: "&4{boss_name}&rが目覚めた..."
  victory: "&6{boss_name}&rを&a{time}&rで討伐！"
  defeat: "&c{boss_name}&rに敗北..."

time-warnings:
  300: "&e残り&c5分&r！"
  60: "&c残り1分！"
  10: "&4残り10秒！"

victory-commands:
  - "give {player} diamond 5"
  - "eco give {player} 10000"

defeat-commands:
  - "say {player}は{boss_name}に敗北した..."
```

## PlaceholderAPI

| プレースホルダー | 説明 |
|-----------------|------|
| `%mofubossbattle_ranking_<boss>_<rank>_name%` | 指定順位のプレイヤー名 |
| `%mofubossbattle_ranking_<boss>_<rank>_time%` | 指定順位のクリアタイム |
| `%mofubossbattle_ranking_<boss>_top_name%` | 1位のプレイヤー名 |
| `%mofubossbattle_ranking_<boss>_top_time%` | 1位のクリアタイム |
| `%mofubossbattle_myrank_<boss>%` | 自分の順位 |
| `%mofubossbattle_mybest_<boss>%` | 自分のベストタイム |
| `%mofubossbattle_in_battle%` | 戦闘中かどうか (true/false) |
| `%mofubossbattle_current_boss%` | 現在のボス名 |
| `%mofubossbattle_time_remaining%` | 残り時間 |
| `%mofubossbattle_time_elapsed%` | 経過時間 |
| `%mofubossbattle_total_clears_<boss>%` | ボスの総クリア人数 |
| `%mofubossbattle_boss_available_<boss>%` | 指定ボスが現在空いているか (true/false)。待機部屋の看板等に設置可能 |
| `%mofubossbattle_boss_status_<boss>%` | 指定ボスの状況テキスト（空き/挑戦中、messages.ymlの`status`セクションで設定） |

待機部屋にサイン(看板)やホログラム等でこれらのプレースホルダーを設置しておくと、プレイヤーはボス部屋に入る前（バフアイテムを使う前）に他のプレイヤーが挑戦中かどうかを確認できます。同様に `/boss status <boss_id>` コマンドでも確認できます。

## 戦闘フロー

1. `/boss start <boss_id>` を実行
2. プレイヤーがテレポート地点に移動
3. MythicMobsでボスをスポーン
4. 戦闘開始、タイマー開始
5. ボス撃破 or 時間切れ/死亡/ログアウト
6. 撃破時: ランキング記録 → 勝利コマンド実行 → アイテム回収時間
7. 回収時間終了後、退出地点へテレポート

## ビルド

```bash
./gradlew build
```

JARファイルは `build/libs/MofuBossBattle-x.x.x.jar` に生成されます。

## ライセンス

Private - MofuCraft

## ビルドとテスト

```sh
./gradlew build      # shadowJar と単体テストまで。JDK 21 が必要
```

(`build/libs/MofuBossBattle-1.0.0.jar` ができます)
