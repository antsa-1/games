<template>
	<div class="row">
		<div class="col">
			<button v-if="resignButtonVisible" :disabled ="resignButtonDisabled" @click="resign" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
				Resign
			</button>			
			<button v-if="rematchButtonEnabled" @click="rematch" type="button" class="btn btn-primary w-30 float-xs-start float-sm-end">
				Rematch
			</button>
		</div>
	</div>

	<div class="row">
		<div class="col-xs-12 col-sm-4">
			Connect 4 table
				<br>
				<input class="form-check-input " type="checkbox" id="highlight" @change="highlightLastSquareIfSelected" v-model="showLastVal">
				<label class="form-check-label ms-1" for="highlight">Highlight last</label>
				<br>
				<input class="form-check-input " type="checkbox" id="soundOn" v-model="soundOn">
				<label class="form-check-label ms-1" for="soundOn">Notification sound</label>
				<br>				
				<span  v-if="theTable.playerA" class="fw-bold" :class="{'text-success':  theTable.playerInTurn?.name=== theTable?.playerA?.name}">
				 	{{theTable?.playerA?.gameToken}} = {{theTable?.playerA?.name}} 
				</span> 		
				<span class="text-primary"> [ w:{{ theTable?.playerA?.wins }} , d:{{ theTable?.playerA?.draws }} ] </span>		
				<br> vs.
				<br>
				 <span v-if="theTable.playerB" class="fw-bold"  :class="{'text-success': theTable.playerInTurn?.name===  theTable?.playerB?.name}">
					 {{theTable?.playerB?.gameToken}} =  {{theTable?.playerB?.name}}   
				</span>
				<span class="text-primary"> [ w:{{ theTable?.playerB?.wins }} , d:{{ theTable?.playerB?.draws }} ]  </span>
			<span v-if="theTable.playerInTurn?.name === userName" class="text-success"> It's your turn. </span>
			<span v-else-if="theTable?.playerInTurn === null" class="text-success"> Game ended </span>	
			<div v-else class="text-danger"> In turn: {{theTable.playerInTurn?.name}}</div>
			
			<div :class="{'hidden': theTable.playerInTurn?.name !== userName}">
				 <span class="text-danger"> Time left {{timeLeft}} </span>
			</div>
		</div>
		<div class="col-xs-12 col-sm-8">
			 <canvas :class="{'bg-secondary':theTable.playerInTurn ==null}" id="canvas" style="border:1px solid #000000;"  ></canvas>
    	</div>
		
	</div>
	<chat :id="theTable.id"> </chat>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {IGameMode,IGameToken, ITable, IUser,ISquare, IWin} from "../interfaces";
import { loginMixin } from "../mixins/mixins";
import { useRoute } from "vue-router";
import Chat from "./Chat.vue";


export default defineComponent({
  components: { Chat },
  name: "Table",
  mixins: [loginMixin],
  props: ["watch"],
  data() {
    return {
      canvas: null,
      renderingContext: null,
      showLastVal: false,
      soundOn: false,
      secondsLeft: 120,
      startingPosX: -1,
      startingPosY: -1,
      gameBoardHeight: -1,
      gameBoardWidth: -1,
      arcDiameter: -1,
      selectedColumn: -1,
    };
  },
  created() {
    this.unsubscribe = this.$store.subscribe((mutation, state) => {
      if (mutation.type === "move") {
        const board: ISquare[] = state.theTable.board;
        this.removeLastSquareHighLightning();
        this.drawToken(this.theTable.board[this.theTable.board.length - 1]);
        this.playMoveNotification();
        this.highlightLastSquareIfSelected();
      } else if (mutation.type === "changeTurn") {
        if (state.theTable.playerInTurn.name === this.userName) {
          this.addMouseListeners();
          this.startReducer();
        } else {
          this.removeMouseListeners();
          this.stopReducer();
        }
      } else if (mutation.type === "rematch") {
        this.initBoard();
      } else if (mutation.type === "updateWinner") {
        const win: IWin = state.theTable.win;
        this.stopReducer();
        this.removeMouseListeners();
        this.drawWinningLine(win);
      }
    });
  },
  computed: {
    timeLeft() {
      return this.secondsLeft;
    },
    theTable(): ITable {
      return this.$store.getters.theTable;
    },
    iconColor() {
      const watchers = this.$store.getters.theTable.watchers;
      if (watchers && watchers.length > 1) {
        return "green";
      }

      return "#000000";
    },
    watchers() {
      return this.$store.getters.theTable.watchers;
    },
    resignButtonDisabled() {
      const table = this.$store.getters.theTable;
      if (this.watch) {
        return true;
      } else if (this.theTable && !this.theTable.playerInTurn) {
        return true;
      } else if (this.theTable.playerInTurn.name !== this.userName) {
        return true;
      } else if (
        this.theTable &&
        this.theTable.board &&
        this.theTable.board.length < 4
      ) {
        return true;
      }

      return false;
    },
    rematchButtonEnabled() {
      const table = this.$store.getters.theTable;
      return !this.watch && table.playerInTurn === null;
    },
    resignButtonVisible() {
      return (
        this.userName === this.theTable?.playerA?.name ||
        this.userName === this.theTable?.playerB?.name
      );
    },
  },
  mounted() {
    if (!this.$store.getters.theTable) {
      this.$router.push("/error");
      return;
    }
    this.initBoard();

    if (this.watch === "1") {
      this.drawBoard();
    }
  },
  beforeUnmount() {
    this.unsubscribe();
    this.leaveTable();
  },
  methods: {
    startReducer() {
      if (this.redurcerInterval) {
        this.stopReducer();
      }
      this.secondsLeft = 120;
      this.redurcerInterval = setInterval(() => {
        this.secondsLeft = this.secondsLeft - 1;
        if (this.secondsLeft <= 0) {
          this.stopReducer();
          this.resign();
        }
      }, 1000);
    },

    stopReducer() {
      clearInterval(this.redurcerInterval);
      this.secondsLeft = null;
    },
    highlightLastSquareIfSelected() {
      if (this.showLastVal && this.theTable.board.length > 1) {
        const board = this.theTable.board;
        const lastSquare: ISquare = board[board.length - 1];
        this.drawToken(lastSquare, "#FF0000");
      }
    },
    playMoveNotification() {
      if (this.soundOn) {
        const audioCtx = new window.AudioContext();
        const oscillator = audioCtx.createOscillator();
        oscillator.type = "sine";
        oscillator.frequency.setValueAtTime(446, audioCtx.currentTime); // value in hertz
        oscillator.connect(audioCtx.destination);
        oscillator.start();
        setTimeout(() => {
          oscillator.stop();
        }, 250);
      }
    },
    removeLastSquareHighLightning() {
      if (this.theTable.board.length > 1) {
        const board = this.theTable.board;
        const lastSquare: ISquare = board[board.length - 2];
        this.drawToken(lastSquare, "#000000");
      }
    },
    removeMouseListeners() {
      this.canvas.removeEventListener("click", this.handleClick, false);
      this.canvas.removeEventListener("mousemove", this.handleCircleIndicator);
    },
    addMouseListeners() {
      this.canvas.addEventListener("click", this.handleClick, false);
      this.canvas.addEventListener("mousemove", this.handleCircleIndicator);
    },
    handleCircleIndicator(event: MouseEvent) {
      const column =
        Math.floor((event.offsetX - this.startingPosX) / this.arcDiameter) + 1;
      if (column != this.column && column > 0 && column <= this.theTable.x) {
        const clearRectangleStartX = this.column * (this.arcDiameter / 2);
        this.renderingContext.clearRect(
          clearRectangleStartX,
          0,
          clearRectangleStartX + this.arcDiameter,
          this.arcDiameter - 3
        );
        this.column = column;
        const xCenter = this.arcDiameter * this.column;
        const yCenter = this.arcDiameter / 2;
        this.renderingContext.fillStyle = "red";
        if (
          this.theTable?.playerInTurn?.gameToken === IGameToken.X &&
          this.userName === this.theTable?.playerInTurn.name
        ) {
          this.renderingContext.fillStyle = "blue";
        }
        this.renderingContext.beginPath();
        this.renderingContext.arc(xCenter, yCenter, 23, 0, 2 * Math.PI);

        this.renderingContext.fill();
      }
    },
    drawWinningLine(win: IWin) {
      this.renderingContext.beginPath();
      const fromX = (win.fromY + 1) * this.arcDiameter;
      const fromY = (win.fromX + 1) * this.arcDiameter + this.arcDiameter / 2;
      const toX = (win.toY + 1) * this.arcDiameter;
      const toY = (win.toX + 1) * this.arcDiameter + this.arcDiameter / 2;
      this.renderingContext.moveTo(fromX, fromY);
      this.renderingContext.lineTo(toX, toY);
      this.renderingContext.strokeStyle = "#00FFFF";
      this.renderingContext.lineWidth = 5;
      this.renderingContext.stroke();
    },
    initBoard() {
      const table: ITable = this.theTable;
      this.canvas = document.getElementById("canvas");
      this.renderingContext = this.canvas.getContext("2d");
      this.canvas.height = 440;
      this.canvas.width = 440;
      if (table.x >= 10 && table.x <= 20) {
        this.canvas.height = 620;
        this.canvas.width = 620;
      } else if (table.x > 20) {
        this.canvas.height = 800;
        this.canvas.width = 800;
      }

      this.arcDiameter = this.canvas.width / (table.x + 1);
      this.drawBoardFrame(table);
      this.drawBoardLines(table);
      if (this.theTable.playerInTurn?.name === this.userName) {
        this.addMouseListeners();
      }
    },
    drawBoardFrame(table: ITable) {
      this.gapBetweenArcs = 28;
      this.gameBoardHeight = (table.y + 1) * this.arcDiameter;
      this.gameBoardWidth = table.x * this.arcDiameter;
      this.startingPosX = this.gapBetweenArcs;
      this.startingPosY = this.arcDiameter;
      this.renderingContext.beginPath();

      this.renderingContext.rect(
        this.startingPosX,
        this.startingPosY,
        this.gameBoardWidth,
        this.gameBoardHeight
      );
      this.renderingContext.stroke();
    },
    drawBoardLines(table: ITable) {
      console.log("drawBoardLines x=" + table.x + "   Y=" + table.y);
      for (let x = 0; x < table.x; x++) {
        for (let y = 0; y < table.y + 1; y++) {
          if (y === 0) {
            continue; //First row is empty for showing 'clickable' token
          }
          const xCenter = this.arcDiameter * x + this.arcDiameter;
          const yCenter = this.arcDiameter * y + this.gapBetweenArcs;
          this.renderingContext.beginPath();
          this.renderingContext.arc(xCenter, yCenter, 25, 0, 2 * Math.PI);
          this.renderingContext.stroke();
        }
      }
    },
    drawBoard() {
      let tokens: ISquare[] = this.theTable.board;
      tokens.forEach((element) => {
        this.drawToken(element);
      });
    },

    drawToken(square: ISquare, color: string) {
      const token: IGameToken = square.token;
      if (token == IGameToken.O) {
        this.renderingContext.fillStyle = "red";
      } else {
        this.renderingContext.fillStyle = "blue";
      }
      const xCenter = this.arcDiameter * (square.x + 1) + this.gapBetweenArcs;
      const yCenter = this.arcDiameter * square.y + this.arcDiameter;
      this.renderingContext.beginPath();
      this.renderingContext.arc(yCenter, xCenter, 23, 0, 2 * Math.PI);
      this.renderingContext.stroke();
      this.renderingContext.fill();
    },
    handleClick(event: MouseEvent) {
      const obj = { title: "MOVE", y: this.column - 1 };
      this.user.webSocket.send(JSON.stringify(obj));
    },
    disableBoard() {
      this.renderingContext.font = "30px Arial";
      this.renderingContext.fillText("Game over", 10, this.canvas.height / 2);
      return true;
    },
    rematch() {
      const obj = { title: "REMATCH", message: "" };
      this.user.webSocket.send(JSON.stringify(obj));
    },
    resign() {
      const obj = { title: "RESIGN", message: this.tablea };
      this.user.webSocket.send(JSON.stringify(obj));
    },
    leaveTable() {
      const obj = { title: "LEAVE_TABLE", message: this.theTable.id };
      this.user.webSocket.send(JSON.stringify(obj));
    },
  },
});
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.hidden {
  visibility: hidden;
}
</style>
