import { IBaseTable } from "@/interfaces/commontypes";
import { ITable, IPlayer } from "@/interfaces/interfaces";
import { IYatzyTable } from "@/interfaces/yatzy";

export const tablesMixin = {
    data() {
        return {
            token: null
        };
    },
    computed: {
        resignButtonDisabled() {
            const table = this.$store.getters.theTable;
            if (this.watch) {

                return true
            } else if (this.theTable && !this.theTable.playerInTurn) {

                return true
            }
            else if (this.theTable.playerInTurn.name !== this.userName) {

                return true
            }
            return false
        },
        rematchButtonEnabled() {
            const table = this.$store.getters.theTable;
            return !this.watch && table.playerInTurn === null
        },
        resignButtonVisible() {
            return this.userName === this.theTable?.playerA?.name || this.userName === this.theTable?.playerB?.name
        },
        theTable(): ITable {
            return this.$store.getters.theTable;
        },
    },
    timeLeft() {
        return this.secondsLeft
    },
    theTable(): ITable {

        return this.$store.getters.theTable;
    },
    watchers() {
        return this.$store.getters.theTable.watchers
    },
    created() {
        if (this.theTable?.playerInTurn?.name === this.userName) {
            this.startTime = 120
            //this.startReducer()
        }
    },
    methods: {
        stopReducer() {
            clearInterval(this.redurcerInterval)
            this.secondsLeft = null
        },
        playMoveNotification() {
            if (this.soundOn) {
                const audioCtx = new (window.AudioContext)();

                const oscillator = audioCtx.createOscillator();

                oscillator.type = "sine";
                oscillator.frequency.setValueAtTime(446, audioCtx.currentTime); // value in hertz

                oscillator.connect(audioCtx.destination);
                oscillator.start();
                setTimeout(
                    () => {
                        oscillator.stop();
                    },
                    250
                );
            }
        },
        removeMouseListeners() {
            this.canvas.removeEventListener("click", this.handleClick, false);
        },
        disableBoard() {
            console.log("disable")
        },
        rematch() {

            const obj = { title: "REMATCH", message: "" }
            this.user.webSocket.send(JSON.stringify(obj));
        },
        resign() {

            const obj = { title: "RESIGN", message: this.tablea }
            this.user.webSocket.send(JSON.stringify(obj));
        },
        leaveTable() {
            const obj = { title: "LEAVE_TABLE", message: this.theTable.tableId }
            this?.user?.webSocket?.send(JSON.stringify(obj))
            this.$store.dispatch("clearTable")
        },
        openTable(table: IBaseTable) {
            this.$router.push({ name: this.getTableName(table.gameMode.gameNumber), id: table.tableId })
        },
        openWatcherTable(table: ITable) {
            this.$router.push({ name: this.getTableName(table.gameMode.gameNumber), id: table.tableId, params: { watch: "1" } })
        },
        getTableName(gameNumber: number): string {
            if (gameNumber === 1) {
                return "TableTicTacToe"
            } else if (gameNumber === 2) {
                return "TableConnectFour"
            }
            else if (gameNumber === 3) {
                return "PoolTable"
            }
            else if (gameNumber === 4) {
                return "YatzyTable"
            }
        },
        getTimeControls() {
            return [{ id: 0, seconds: 120 }, { id: 1, seconds: 90 }, { id: 2, seconds: 60 }, { id: 3, seconds: 45 }, { id: 4, seconds: 30 }, { id: 5, seconds: 20 }]
        },
        isPlayerInTurn(userName: string) {
            return this.theTable.playerInTurn.name === userName
        },
        isTurnChange(nextPlayerName: string) {
            return this.$store.getters.playerInTurn.name !== nextPlayerName
        },
        isTurnChangeToMe(tableFromServer: ITable) {
            return tableFromServer.playerInTurn.name === this.userName && this.$store.getters.playerInTurn.name !== this.userName
        },
        isTurnChangeFromMe(tableFromServer: ITable) {
            return tableFromServer.playerInTurn.name !== this.userName && this.$store.getters.playerInTurn.name === this.userName
        },
        isTurnChangeToMe2(nextPlayerName: string) {
            console.log("isTurnChangeToMe2 nextPlayer:" + nextPlayerName + "  storePlayerinInTurn " + this.$store.getters.playerInTurn.name + "  myName:" + this.userName)
            return nextPlayerName === this.userName && this.$store.getters.playerInTurn.name !== this.userName
        },
        shouldChangeTurn(playerInTurn: IPlayer) {
            if (!this.$store.getters.playerInTurn) {
                return false
            }
            if (this.$store.getters.playerInTurn.name === playerInTurn.name) {
                return false
            }
            return true
        },
        async changeTurnIfRequired(playerInTurn: IPlayer) {
            if (this.shouldChangeTurn(playerInTurn)) {
                await this.$store.dispatch("changeTurn", playerInTurn)
            }
        },
        isMe(userName: string) {
            return this.userName === userName
        },
        isMyTurnInStore() {
            return this.theTable?.playerInTurn?.name === this.userName
        },
        isPlayingInTable(table: IBaseTable) {
            if (table.gameMode.gameNumber < 4) {              
                return table.playerA.name === this.userName || table.playerB.name === this.userName
            }
            const yatzyTable:IYatzyTable = <IYatzyTable> table
            return yatzyTable.players.filter(player => player.name === this.userName).length > 0
        },
        playNotificationSound() {

            const audioCtx = new window.AudioContext()
            const oscillator = audioCtx.createOscillator()
            oscillator.type = "sine";
            oscillator.frequency.setValueAtTime(446, audioCtx.currentTime); // value in hertz
            oscillator.connect(audioCtx.destination)
            oscillator.start()
            setTimeout(() => {
                oscillator.stop()
            }, 500)
        },
    },
};