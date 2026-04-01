# Shared Package Guide (Deliverable A)

Αυτό το αρχείο εξηγεί **τι κάνει κάθε `.java` αρχείο** μέσα στο `backend/shared/` .

> **Σημαντικό:** Το `shared/` περιέχει **μόνο κοινά DTOs / enums / protocol definitions** και **δεν** περιέχει business logic, socket code ή worker/master logic.



# 1) `Message.java`

## Τι είναι

Το **βασικό envelope/wrapper** όλων των μηνυμάτων που ταξιδεύουν με TCP sockets.

## Ρόλος

Κάθε επικοινωνία στο σύστημα (Manager, Player, Master, Worker, SRG) πρέπει να στέλνεται μέσα σε ένα `Message` object.

## Περιέχει

* `MessageType type` → τι είδους μήνυμα είναι
* `MessageSource source` → ποιος το έστειλε
* `Object payload` → τα πραγματικά δεδομένα του request/response

## Παράδειγμα

* `ADD_GAME` με payload ένα `Game`
* `PLAY_GAME` με payload ένα `PlayRequest`
* `ACK` με payload ένα `AckResponse`

## Ποιος το χρησιμοποιεί

* Όλοι: Manager, Dummy Client, Master, Worker, SRG

---

# 2) `MessageType.java`

## Τι είναι

`enum` που ορίζει **όλες τις επιτρεπόμενες εντολές και responses** του Deliverable A.

## Ρόλος

Είναι το **κοινό protocol contract** που πρέπει να ακολουθούν όλα τα μέλη.

## Παραδείγματα τιμών

* `ADD_GAME`
* `SEARCH_GAMES`
* `PLAY_GAME`
* `STORE_GAME`
* `SEARCH_LOCAL`
* `PLAY_RESULT`
* `RANDOM_REQUEST`
* `ACK`
* `ERROR`

## Ποιος το χρησιμοποιεί

* Όλοι, ειδικά Master και Worker για routing/dispatching

---

# 3) `MessageSource.java`

## Τι είναι

`enum` που δείχνει **από ποιο component προήλθε** το μήνυμα.

## Πιθανές τιμές

* `MANAGER`
* `PLAYER`
* `MASTER`
* `WORKER`
* `SRG`
* `REDUCER`

## Ρόλος

Χρήσιμο για:

* validation
* debugging
* logging
* πιο καθαρό handling στον Master

---

# 4) `RiskLevel.java`

## Τι είναι

`enum` για το επίπεδο ρίσκου ενός παιχνιδιού.

## Τιμές

* `LOW`
* `MEDIUM`
* `HIGH`

## Ρόλος

Χρησιμοποιείται:

* μέσα στο `Game`
* στο `SearchRequest`
* στο `EditRiskRequest`
* στον Worker betting logic (Member 2)

---

# 5) `BetCategory.java`

## Τι είναι

`enum` για την κατηγορία πονταρίσματος.

## Τιμές

* `LOW_BET`
* `MEDIUM_BET`
* `HIGH_BET`

## Ρόλος

Η εκφώνηση λέει ότι **δεν έρχεται από JSON**, αλλά **υπολογίζεται** από τα `minBet` / `maxBet`.

Χρησιμοποιείται:

* στο `Game`
* στο `GameSummary`
* στο `SearchRequest`

---

# 6) `Game.java`

## Τι είναι

Το **βασικό domain object** για κάθε παιχνίδι καζίνο.

## Περιέχει βασικά στοιχεία

* `gameName`
* `providerName`
* `stars`
* `noOfVotes`
* `gameLogo`
* `minBet`
* `maxBet`
* `riskLevel`
* `hashKey`
* `secret`

## Περιέχει και derived στοιχεία

* `betCategory`
* `jackpot`

## Ρόλος

Χρησιμοποιείται ως payload όταν:

* ο Manager κάνει `ADD_GAME`
* ο Master στέλνει `STORE_GAME` στον Worker

## Σημείωση

Το `secret` είναι σημαντικό για την επικοινωνία Worker ↔ SRG (Member 3).

---

# 7) `GameSummary.java`

## Τι είναι

Ελαφρύτερο DTO για να επιστρέφονται αποτελέσματα αναζήτησης στον player.

## Γιατί υπάρχει

Δεν χρειάζεται ο player να πάρει ολόκληρο το `Game` object.
Συνήθως στο search αρκούν:

* όνομα
* provider
* stars
* logo
* min/max bet
* risk level
* bet category
* jackpot

## Ρόλος

Χρησιμοποιείται μέσα στο `SearchResultResponse`.

---

# 8) `WorkerInfo.java`

## Τι είναι

DTO που περιγράφει έναν Worker node.

## Περιέχει

* `workerId`
* `host`
* `port`

## Ρόλος

Το χρησιμοποιεί ο Master για:

* worker list
* routing με `H(GameName) mod n`
* σύνδεση με σωστό Worker μέσω socket

## Ποιος το χρησιμοποιεί κυρίως

* Member 1 (Master)

---

# 9) `SearchRequest.java`

## Τι είναι

Request DTO για την εντολή `search()` του player/dummy client.

## Περιέχει φίλτρα

* `playerId`
* `minStars`
* `riskLevel`
* `betCategory`

## Ρόλος

Flow:

1. Player → Master (`SEARCH_GAMES`)
2. Master → όλοι Workers (`SEARCH_LOCAL`)
3. Workers επιστρέφουν partial results
4. Master κάνει merge και απαντά στον player

## Ποιος το χρησιμοποιεί

* Member 1, Member 2, Member 4

---

# 10) `PlayRequest.java`

## Τι είναι

Request DTO για την εντολή `play()`.

## Περιέχει

* `playerId`
* `gameName`
* `amount`

## Ρόλος

Flow:

1. Player → Master (`PLAY_GAME`)
2. Master επιλέγει Worker με hash
3. Master → Worker (`PLAY_LOCAL`)
4. Worker καλεί SRG
5. Worker επιστρέφει `PLAY_RESULT`

## Ποιος το χρησιμοποιεί

* Member 1, Member 2, Member 4

---

# 11) `AddBalanceRequest.java`

## Τι είναι

Request DTO για την εντολή `addBalance()`.

## Περιέχει

* `playerId`
* `amount`

## Ρόλος

Χρησιμοποιείται όταν ο player φορτώνει χρήματα στον λογαριασμό του.

## Προτεινόμενη υλοποίηση στο Deliverable A

Για απλότητα:

* το balance να αποθηκεύεται στον **Master**
* όχι distributed στους Workers

## Ποιος το χρησιμοποιεί

* Member 1, Member 4

---

# 12) `RateGameRequest.java`

## Τι είναι

Request DTO για rating παιχνιδιού με αστέρια.

## Περιέχει

* `playerId`
* `gameName`
* `rating` (1 έως 5)

## Ρόλος

Flow:

1. Player → Master (`RATE_GAME`)
2. Master βρίσκει Worker με hash(gameName)
3. Master → Worker (`RATE_GAME_LOCAL`)
4. Worker ενημερώνει `stars` και `noOfVotes`

## Ποιος το χρησιμοποιεί

* Member 1, Member 2, Member 4

---

# 13) `RemoveGameRequest.java`

## Τι είναι

Request DTO για αφαίρεση παιχνιδιού από τον Manager.

## Περιέχει

* `gameName`

## Ρόλος

Flow:

1. Manager → Master (`REMOVE_GAME`)
2. Master βρίσκει Worker με hash(gameName)
3. Master → Worker (`REMOVE_GAME_LOCAL`)

## Ποιος το χρησιμοποιεί

* Member 1, Member 2, Member 4 (Manager app)

---

# 14) `EditRiskRequest.java`

## Τι είναι

Request DTO για αλλαγή `riskLevel` ενός παιχνιδιού.

## Περιέχει

* `gameName`
* `newRiskLevel`

## Ρόλος

Flow:

1. Manager → Master (`EDIT_GAME_RISK`)
2. Master βρίσκει σωστό Worker
3. Master → Worker (`UPDATE_GAME_RISK_LOCAL`)

## Ποιος το χρησιμοποιεί

* Member 1, Member 2, Member 4 (Manager app)

---

# 15) `SearchResultResponse.java`

## Τι είναι

Response DTO για αποτελέσματα αναζήτησης.

## Περιέχει

* `List<GameSummary> games`

## Ρόλος

Χρησιμοποιείται:

* από Worker προς Master για partial search results
* από Master προς Player για τελικό merged αποτέλεσμα

## Σημείωση

Στο Deliverable A μπορεί να χρησιμοποιηθεί και για partial και για final result για απλότητα.

---

# 16) `PlayResultResponse.java`

## Τι είναι

Response DTO για το αποτέλεσμα του `play()`.

## Περιέχει

* `win` → αν κέρδισε ο παίκτης
* `payout` → πόσα χρήματα επιστρέφονται
* `newBalance` → νέο υπόλοιπο
* `message` → human-readable μήνυμα/debug info

## Ρόλος

Flow:

* Worker → Master (`PLAY_RESULT`)
* Master → Player (`PLAY_RESULT`)

## Ποιος το χρησιμοποιεί

* Member 1, Member 2, Member 4

---

# 17) `RandomRequest.java`

## Τι είναι

Request DTO για επικοινωνία Worker → SRG.

## Περιέχει

* `gameName`
* `playerId`
* `workerNonce`

## Ρόλος

Ο Worker ζητά έναν ασφαλή τυχαίο αριθμό από τον SRG server.

## Γιατί υπάρχει

Ο SRG είναι ξεχωριστή TCP υπηρεσία (Member 3), άρα χρειάζεται κοινό request format.

## Ποιος το χρησιμοποιεί

* Member 2, Member 3

---

# 18) `RandomResponse.java`

## Τι είναι

Response DTO από SRG προς Worker.

## Περιέχει

* `randomNumber`
* `sha256Hash`
* `message`

## Ρόλος

Ο Worker παίρνει:

* τον τυχαίο αριθμό
* το SHA256 proof για verification

## Ποιος το χρησιμοποιεί

* Member 2, Member 3

---

# 19) `AckResponse.java`

## Τι είναι

Γενικό success response.

## Περιέχει

* `success`
* `message`

## Ρόλος

Χρησιμοποιείται όταν μια ενέργεια ολοκληρώνεται σωστά, π.χ.:

* add game
* remove game
* edit risk
* add balance
* rate game

## Γιατί είναι χρήσιμο

Αντί να φτιάχνουμε ξεχωριστό response για κάθε απλή επιτυχία, χρησιμοποιούμε ένα κοινό object.

---

# 20) `ErrorResponse.java`

## Τι είναι

Γενικό error response.

## Περιέχει

* `errorCode`
* `message`

## Ρόλος

Χρησιμοποιείται όταν κάτι πάει λάθος, π.χ.:

* game not found
* insufficient balance
* invalid bet amount
* invalid rating
* worker unavailable

## Γιατί είναι χρήσιμο

Δίνει κοινό format για όλα τα errors, άρα όλα τα μέλη μπορούν να τα χειρίζονται με τον ίδιο τρόπο.

---

# 🔁 Βασική Συμφωνία Ομάδας (Important)

Όλα τα μέλη πρέπει να ακολουθούν αυτό το pattern:

1. **Κάθε socket message είναι πάντα `Message`**
2. Το `type` δείχνει ποια ενέργεια γίνεται
3. Το `source` δείχνει ποιος έστειλε το μήνυμα
4. Το `payload` είναι ένα από τα shared DTOs

---

# 🧩 Παράδειγμα Flows

## Add Game

* Manager → Master: `Message(ADD_GAME, MANAGER, Game)`
* Master → Worker: `Message(STORE_GAME, MASTER, Game)`
* Worker → Master: `Message(ACK, WORKER, AckResponse)`

## Search Games

* Player → Master: `Message(SEARCH_GAMES, PLAYER, SearchRequest)`
* Master → Workers: `Message(SEARCH_LOCAL, MASTER, SearchRequest)`
* Worker → Master: `Message(SEARCH_RESULT_PARTIAL, WORKER, SearchResultResponse)`
* Master → Player: `Message(SEARCH_GAMES, MASTER, SearchResultResponse)`

## Play Game

* Player → Master: `Message(PLAY_GAME, PLAYER, PlayRequest)`
* Master → Worker: `Message(PLAY_LOCAL, MASTER, PlayRequest)`
* Worker → SRG: `Message(RANDOM_REQUEST, WORKER, RandomRequest)`
* SRG → Worker: `Message(RANDOM_RESPONSE, SRG, RandomResponse)`
* Worker → Master: `Message(PLAY_RESULT, WORKER, PlayResultResponse)`
* Master → Player: `Message(PLAY_RESULT, MASTER, PlayResultResponse)`

---

# 👥 Ποιος χρειάζεται τι

## Member 1 (Master)

Χρειάζεται σχεδόν **όλα** τα shared αρχεία.

## Member 2 (Worker)

Χρειάζεται:

* `Game`
* `PlayRequest`
* `RateGameRequest`
* `SearchRequest`
* `SearchResultResponse`
* `PlayResultResponse`
* `RandomRequest`
* `RandomResponse`
* `AckResponse`
* `ErrorResponse`
* `Message`, `MessageType`, `MessageSource`

## Member 3 (SRG)

Χρειάζεται κυρίως:

* `Message`
* `MessageType`
* `MessageSource`
* `RandomRequest`
* `RandomResponse`

## Member 4 (Manager + Dummy Client)

Χρειάζεται:

* `Game`
* `SearchRequest`
* `PlayRequest`
* `AddBalanceRequest`
* `RateGameRequest`
* `RemoveGameRequest`
* `EditRiskRequest`
* `SearchResultResponse`
* `PlayResultResponse`
* `AckResponse`
* `ErrorResponse`
* `Message`, `MessageType`, `MessageSource`

---

