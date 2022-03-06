<!--
 * @author antsa-1 from GitHub 
 -->
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
			Text
		</div>
		<div class="col-xs-12 col-sm-8">
			 <canvas  id="canvas" ></canvas>
    	</div>
		
	</div>
	<chat :id="theTable.id"> </chat>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {IGameMode,IGameToken,	ITable,	IUser,ISquare, IWin} from "../../interfaces";
import { loginMixin } from "../../mixins/mixins";
import { useRoute } from "vue-router";
import Chat from "../Chat.vue";


export default defineComponent({
	components: { Chat },
	name: "PoolTable",
	mixins: [loginMixin],
	props:["watch"],
	data(){
		return{
			horizontalGap: null,
			verticalGap: null,
			canvas:null,
			renderingContext:null,
			showLastVal:false,
			soundOn:false,
			secondsLeft:120,
		}
	},
	created() {
		
		this.unsubscribe = this.$store.subscribe((mutation, state) => {
			if (mutation.type === "move") {
				const board : ISquare[]= state.theTable.board
				this.removeLastSquareHighLightning()
				this.drawToken(this.theTable.board[this.theTable.board.length -1])
				this.playMoveNotification()
				this.highlightLastSquareIfSelected()
			}else if (mutation.type === "changeTurn") {
				if(state.theTable.playerInTurn.name === this.userName){
					this.addMouseListener()
					this.startReducer()
				}else{
					this.removeMouseListener()
					this.stopReducer()
				}
			}else if (mutation.type === "rematch" ){			
				this.initBoard()			
			}
			else if (mutation.type === "updateWinner" ){			
				console.log("update winner")
			}else if(mutation.type === "setDraw"){
				this.stopReducer()
				this.removeMouseListener()
			}
    	})
		if(this.theTable.playerInTurn?.name===this.userName){
			this.startTime=120
			this.startReducer()
		}
	},
	computed: {
		timeLeft(){
			return this.secondsLeft
		},
		theTable() :ITable{
			
			return this.$store.getters.theTable;
		},
		iconColor(){
		 	const watchers=this.$store.getters.theTable.watchers
			 if(watchers && watchers.length>1){
				 return "green"
			 }

			 return "#000000"
		},
		watchers(){
			return this.$store.getters.theTable.watchers
		},
		resignButtonDisabled(){
			const table=this.$store.getters.theTable;
			if(this.watch){
			
				return true
			}else if(this.theTable && !this.theTable.playerInTurn){
					
				return true
			}
			else if(this.theTable.playerInTurn.name!==this.userName){
					
				return true
			}else if(this.theTable && this.theTable.board&& this.theTable.board.length<4){
					
				return true
			}
				
			return false
		},
		rematchButtonEnabled(){
				const table=this.$store.getters.theTable;
			 return !this.watch && table.playerInTurn ===null
		},
		resignButtonVisible(){
			return this.userName===this.theTable?.playerA?.name || this.userName===this.theTable?.playerB?.name
		}
	
	},
	mounted() {
		if(!this.$store.getters.theTable){
			
			this.$router.push('/error')
			return;
		}
		this.initBoard()
		
		if(this.watch==="1"){
			this.drawBoard()
		}
	},
	beforeUnmount() {
    	this.unsubscribe()
		this.leaveTable()
  	},
	methods: {
		startReducer(){			
			if(this.redurcerInterval){
				this.stopReducer()
			}
			this.secondsLeft = 120;
			this.redurcerInterval= setInterval(()=>{
				this.secondsLeft = this.secondsLeft-1
				if(this.secondsLeft <= 0){
					this.stopReducer()
					this.resign()
				}
			}, 1000)
		},
		
		stopReducer(){
			clearInterval(this.redurcerInterval)
			this.secondsLeft=null
		},
	
		playMoveNotification(){
			if(this.soundOn){
				const audioCtx = new (window.AudioContext )();

				const oscillator = audioCtx.createOscillator();

				oscillator.type = "sine";
				oscillator.frequency.setValueAtTime(446, audioCtx.currentTime); // value in hertz
				
				oscillator.connect(audioCtx.destination);
				oscillator.start();
				setTimeout(
					()=> {
					oscillator.stop();
					},
					250
				);
			}
		},
		removeLastSquareHighLightning(){
			if(this.theTable.board.length>1){
				
				const board=this.theTable.board
				const lastSquare:ISquare=board[board.length-2]
				this.drawToken(lastSquare,"#000000")	
			}
		},
		removeMouseListener(){
			this.canvas.removeEventListener("click", this.handleClick, false);
		},
		addMouseListener(){
			this.canvas.addEventListener("click", this.handleClick, false);
		},
		drawWinningLine(win:IWin){
			
			console.log("win")
		},
		initBoard() {
			let table: ITable = this.theTable
			
		},
		drawBoard(){
			
		 	let tokens:ISquare[]=this.theTable.board
			tokens.forEach(element => {
				this.drawToken(element)
			});
		},
	
		drawToken(square: ISquare,color:string) {
			
			
		},
		handleClick(event: MouseEvent) {
			console.log("click")
		},
		disableBoard(){
				console.log("disable")
		},
		rematch(){
			
			const obj ={title:"REMATCH", message:""}
			this.user.webSocket.send(JSON.stringify(obj));	
		},
		resign(){
			
			const obj ={title:"RESIGN", message:this.tablea}
			this.user.webSocket.send(JSON.stringify(obj));	
		},
		leaveTable(){
			const obj ={title:"LEAVE_TABLE", message:this.theTable.tableId}
			this?.user?.webSocket?.send(JSON.stringify(obj));
		}
	},
	
});
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.hidden{
	visibility:hidden
}
</style>
