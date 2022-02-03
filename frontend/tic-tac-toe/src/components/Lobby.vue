<template>
	<div class="row mt-3">
		<div class="col-xs-12 col-sm-4 mt-3 mt-md-0 pt-3">
			<span class="fw-bold ">
				Players
			</span>		
			<ul class="list-group ">						
				<li v-for="(user, index) in users" :key="user.name" class="list-group-item" :class="[index%2==0?'bg-success p-2 text-dark bg-opacity-25':'bg-success p-2 text-dark bg-opacity-10']">					
					<div class="float-start ">
						{{user.name}}
						
						<svg v-if="user.name===userName" xmlns="http://www.w3.org/2000/svg" width="23" height="23" fill="currentColor" class="bi bi-arrow-left" viewBox="0 0 16 16">
  							<path fill-rule="evenodd" d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z"/>
						</svg>
					</div>
				</li>
			</ul>
		</div>
		<div class="col-xs-12 col-sm-4 mt-3 mt-md-0">
			<div class="row">
				<div class="col-xs-6">
					<span class="fw-bold ">
						Tables
					</span>
					<span class="float-end">
						<button v-if="isCreateTableButtonVisible" type="button" class="btn btn-primary w-30 float-start" data-bs-toggle="modal" data-bs-target="#createTableModal">
							Create table
						</button>
						<button v-if="isRemoveTableButtonVisible" @click="removeTable()" type="button" class="btn btn-primary bg-danger w-30 float-start" >
							Remove table
						</button>
					</span>
				</div>
				
			</div>			
			<div class="row">
				
				<table class="table" v-if="tablesExist" >
				
					
						<thead>
							<tr>							
								<th scope="col">Type</th>
								<th scope="col">Playing</th>
								<th scope="col">Size</th>
								<th scope="col">Action</th>
							</tr>
						</thead>
						<tbody>						
							<tr v-for="(table, index) in tables" :key="table.id" >						
								<td v-if="table.gameMode.id<20" scope="row">
									X.O 
								</td>								
								<td v-else-if="table.gameMode.id>=20" scope="row">
								 4x
								</td>
								<td>
									{{table.playerA.name}} - {{table.playerB?.name}}
								</td>
								<td>
									Con {{table.gameMode.requiredConnections}}
								</td>
								<td>
									<section v-if="!isOwnTable(table)">
										<button v-if="playButtonVisible(table)"  :disabled="!createTableButtonVisible" @click="play(table)" type="button" class="btn btn-primary w-30 float-start">
											Play
										</button>									
										<button @click="watchTable(table)" :disabled="!createTableButtonVisible || !table.playerA || !table.playerB" type="button" class="btn btn-primary w-30 float-end">
											Watch
										</button>	
									</section>
								</td>
							</tr>								
						</tbody>
					</table>
				
				<div v-else class="col fw-bold">
					<span class="float-start"> 
						No tables at the moment. 
					</span>
				</div>
			</div>
		</div>
	
	</div>
	<div class="modal fade" id="createTableModal" tabindex="-1" aria-labelledby="createTableModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title" id="createTableModalLabel">Create table</h5>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				</div>
				<div class="modal-body">
						<form>
							<div class="btn-group" role="group" aria-label="Select game" id="select-game">
								<input type="radio" v-model="selectedGame" value="1" @change="changeSelectedGame"  id="tictactoe" name="tictactoe" class="btn ">
								<label class="btn " for="tictactoe">TicTacToe</label>
								<input type="radio" v-model="selectedGame" value="2" @change="changeSelectedGame" id="connect4" name="connect4" class="btn ">	
								<label class="btn " for="connect4">Connect4</label>						
							</div>
							<div class="mb-3" id="v-model-select-dynamic">						
							<select v-model="selectedGameMode">
								<option :value="0" :key="0" >
									Select board size
								</option>
								<option v-for="gameMode in gameModes" :value="gameMode.id" :key="gameMode.id">
									{{gameMode.name}} connect {{gameMode.requiredConnections}}
								</option>
							</select>			
						</div>
						<div class="mb-3" id="computer_selection">
							<input class="form-check-input mr-2" type="checkbox" id="computerSelection" @change="computerSelectionOnChanged" v-model="playAgainstComputerChecked">
							<label class="form-check-label" for="computerSelection">Play against computer</label>
							
						</div>						
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary float-start" data-bs-dismiss="modal">Close</button>
					<button :disabled="modalCreateTableDisabled" @click="createTable" type="button" data-bs-dismiss="modal" class="btn btn-primary">
						Create table
					</button>
				</div>
			</div>
		</div>
	</div>


</template>

<script lang="ts">
import { defineComponent } from "vue";
import { IGameMode, IGameToken, ITable, IUser,ISquare, IChatMessage, IWinMessage,IWinTitle,IWin, IGameResult } from "../interfaces";
import { loginMixin } from "../mixins/mixins";
import { useRoute } from "vue-router";
import Chat from "./Chat.vue";
export default defineComponent({
	
	name: "Lobby",

	mixins: [loginMixin],

	data() {
		return {		
			createTableButtonVisible:true,
			watchTableButtonDisabled:false,
			removeTableButtonVisible:false,
			selectedGame:"1",
			selectedGameMode:"0",
			playAgainstComputerChecked:false,
			computerLevel:"0",
		}
	},
	computed:{
	
		users() {
			return this.$store.getters.users
		},
		tables(){
			return this.$store.getters.tables
		},
		gameModes(){
			if(this.selectedGame==="1"){
				return this.$store.getters.gameModes.filter(gameMode => gameMode.gameId===1)
			}
			return this.$store.getters.gameModes.filter(gameMode => gameMode.gameId===2)
		},
		modalCreateTableDisabled(){
			return this.selectedGameMode === "0"
			
		},
		tablesExist(){			
			return this.$store.getters.tables.length > 0
		},
		isCreateTableButtonVisible(){
			const firstTable:ITable = this.$store.getters.tables[0]
			if(firstTable && firstTable.playerA){
				return firstTable.playerA.name!==this.userName
			}
			return true
		},
		isRemoveTableButtonVisible(){
			const firstTable:ITable = this.$store.getters.tables[0]
			if(firstTable && firstTable.playerA){
				return firstTable.playerA.name===this.userName
			}
			return false
		},		
	},
	created() {
		
		if (this.user && !this.user.webSocket) {
			
			this.connect(this.user.token);
		}else{
			
		}
	},
	methods: {
		connect(token: string): void {		
			let websocket = new WebSocket(process.env.VUE_APP_WS_URL);

			websocket.onopen = event => {
				const title2 = "LOGIN";
				const obj = { title: title2, message: this.user.token };
				const myJSON = JSON.stringify(obj);
				const loginMessage = JSON.stringify(obj);
				websocket.send(loginMessage);
			};
			websocket.onmessage = event => {
				let data = JSON.parse(event.data);

				switch (data.title) {
					case "LOGIN":
						
						let user: IUser = {
							name: data.to,
							token: data.token,
							webSocket: websocket
						};
					 	this.$store.dispatch("setUser", user).then(() => {
							this.$store.dispatch("setUsers", data.players)
							this.$store.dispatch("setGameModes", data.gameModes)
							this.$store.dispatch("setTables", data.tables)
						})	
						break;
					case "CREATE_TABLE":
						
						this.$store.dispatch("addTable", data.table)						
						if (data.table.playerA.name===this.userName){
							
							this.createTableButtonVisible=false;
							this.removeTableButtonVisible =true
						}
						break;

					case "REMOVE_PLAYER":
						this.$store.dispatch("leaveTable", data.who.name).then(()=>{
							this.$store.dispatch("removePlayer", data.who)
							if(data.table){
							this.$store.dispatch("removeTable", data.table)
						}
						})
					
						break;
					case "REMOVE_TABLE":							
						this.$store.dispatch("removeTable", data.table)
						const tablesb:ITable=data.table
						if (tablesb.playerA && tablesb.playerA.name===this.userName){
							this.createTableButtonVisible=true;
							this.removeTableButtonVisible =false
						}
						break;
					case "NEW_PLAYER":
						
						this.$store.dispatch("addPlayer", data.who)						
						break;
					case "START_GAME":
						
						this.$store.dispatch("startGame", data.table)
						const tableC:ITable=data.table
						if (tableC.playerA.name===this.userName || tableC.playerB.name===this.userName ){
							tableC.playerA.wins=0
							tableC.playerB.wins=0
							this.createTableButtonVisible = false
							this.removeTableButtonVisible = false
							this.$store.dispatch("selectTable", data.table).then(() => {
								if(tableC.gameMode.id>=20){
									console.log("Opening Connect4Table")
									this.$router.push({ name: 'TableCon4', id:data.table.id})
									return
								}
								this.$router.push({ name: 'Table', id:data.table.id})
							})
						}						
						break;
					case "MOVE":
						const square :ISquare = {x: data.x, y: data.y, coordinates: data.x.toString().concat(data.y.toString()), token:data.message}
						
						this.$store.dispatch("move", square)
						this.$store.dispatch("changeTurn", data.table.playerInTurn.name)
						break;
					case "WATCH":						
						this.$store.dispatch("selectTable", data.table).then(() => {
							this.$router.push({ name: 'Table', params: { watch: "1" } })
						})		
						break;
					case "ADD_WATCHER":						
						this.$store.dispatch("addWatcher", data.who).then(() => {
							
						})		
						break;				
					case "CHAT":
						const message:IChatMessage={text: data.message,from:data.from}
						if(data.to==="COMMON"){
							this.$store.dispatch("updateCommonChat", message)
							return
						}
						this.$store.dispatch("chat", message)
						break;
					case "REMATCH":
						this.$store.dispatch("rematch", data.table).then(() => {
							//this.$router.push({ name: 'Table', id:data.table.id})
						})
						break;
					case "LEAVE_TABLE":		 				
						this.$store.dispatch("leaveTable", data.who.name)
						break;
					case "GAME_END":
							const lastSquare :ISquare = {x: data.x, y: data.y, coordinates: data.x.toString().concat(data.y.toString()), token:data.message}							
							this.$store.dispatch("move", lastSquare)							
							if(data.win.draw){
								const gameResult:IGameResult={table:data.table,win:data.win}
								
								this.$store.dispatch("setDraw",gameResult);
							}
							else
							{
								const updateScoreMessage:IWinMessage =
								{
									winner:data.win.player.name,
									reason: IWinTitle.GAME,
									winsA:data.table.playerA.wins,
									winsB:data.table.playerB.wins,
									from:"System"
								}									
								this.$store.dispatch("updateScore", updateScoreMessage);
								const win:IWin={
									fromX:data.win.fromX,
									fromY:data.win.fromY,
									toX:data.win.toX,
									toY:data.win.toY,
									winner:{name:data.win.player.name},	
								}							
								this.$store.dispatch("updateWinner", win);
							}		
						break;
					case "WINNER":
						const winMessage:IWinMessage={
							winner:data.who.name,
							reason:data.reason,
							winsA:data.table.playerA.wins,
							winsB:data.table.playerB.wins,
							from:data.from
						}
						this.$store.dispatch("updateScore", winMessage);
						const title: IWinTitle = data.message ==="R"?IWinTitle.RESIGNITION:IWinTitle.GAME
						let chatText = data.who.name.concat( title===IWinTitle.RESIGNITION? " won by resignation": " won")
               			const chatMessag:IChatMessage = {
							from:data.from,
							text:chatText
						}
						this.$store.dispatch("chat", chatMessag);
						break;
                }
			};
			websocket.onerror = event => {
				
			};
			websocket.onclose = event => {
				
				if(this.user){
					this.user.webSocket=null
				}
			//	this.logout();
			};
		},
		createTable(){
		 	
			const obj = { title: "CREATE_TABLE", message: this.selectedGameMode,"computer":this.playAgainstComputerChecked};			
			this.user.webSocket.send(JSON.stringify(obj));
			this.computerLevel=null;
			this.playAgainstComputerChecked=false;
		},
		computerSelectionOnChanged(){
			if(!this.playAgainstComputerChecked){
				
				this.computerLevel="0";
			}
		},
		changeSelectedGame(event){			
			this.selectedGameMode="0"
		},
		removeTable(){
			
			const obj = { title: "REMOVE_TABLE",message:""};
			this.user.webSocket.send(JSON.stringify(obj));
		},
		play(table:ITable){
			
			const obj = { title: "JOIN_TABLE", message:table.id};
			this.user.webSocket.send(JSON.stringify(obj));
		},
		playButtonVisible(table:ITable){
			if(table.playerA && table.playerB){
				return false				
			}			
			return true
		},
		isOwnTable(table:ITable){
			return	table?.playerA?.name === this.userName
		},
		watchTable(table:ITable){
			
			const obj = { title: "WATCH",message:table.id};
			this.user.webSocket.send(JSON.stringify(obj));
		},
	}
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.players{
	background-color:#8EF0BF
}


</style>
