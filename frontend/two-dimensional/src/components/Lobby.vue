<template>
	<div class="row mt-3">
		<div class="col-xs-12 col-sm-4 mt-3 mt-md-0 pt-3">
			<span class="fw-bold ">
				Players
			</span>		
			<ul class="list-group ">						
				<li v-for="(user, index) in users" :key="user.name" @click="openProfile(user.name)" class="list-group-item bg-success p-2 text-dark" :class="[index%2==0?'bg-opacity-25':' bg-opacity-10', user.name.startsWith(GUEST)?'':'games-profile-link' ] ">					
					<div class="float-start" >
						{{user.name}}						
						<svg v-if="user.name===userName" xmlns="http://www.w3.org/2000/svg" width="23" height="23" fill="currentColor" class="bi bi-arrow-left" viewBox="0 0 16 16">
  							<path fill-rule="evenodd" d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z"/>
						</svg>
					</div> 	
					<i v-if="user.rankingConnectFour>0" class="bi bi-star float-end tooltipselector" title="ConnectFour ranking">
						&nbsp;{{fixVal(user.rankingConnectFour)}}&nbsp;
					</i>
					<i v-if="user.rankingTictactoe>0" class="bi bi-suit-diamond float-end tooltipselector" title="TicTacToe ranking">
						&nbsp;{{fixVal(user.rankingTictactoe)}}&nbsp;
					</i>
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
						<button v-if="!hasCreatedTable" type="button" class="btn btn-primary w-30 float-start" data-bs-toggle="modal" data-bs-target="#createTableModal">
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
							<th scope="col">Table</th>
							<th scope="col">Board</th>
							<th scope="col">Playing</th>
							<th scope="col">Starter</th>
							<th scope="col">Time</th>
							<th scope="col">Action</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="(table) in tables" :key="table.id" >						
							<td scope="row">
								{{getTableShortDesc(table.gameMode.id)}}
							</td>
							<td>
								{{getBoardDesc(table)}}									
							</td>							
							<td>
								{{table.playerA.name}} - {{table.playerB?.name}}
							</td>
							<td>
								{{table.randomizeStarter? "?": "fixed"}} 
							</td>
							<td>
								{{getTimeControl(table)}}s
							</td>						
							<td>
								<section v-if="!hasCreatedTable">
									<button v-if="playButtonVisible(table)" :disabled="!createTableButtonVisible" @click="play(table)" type="button" class="btn btn-primary w-30 float-start">
										Play
									</button>
																
									<button @click="watchTable(table)" v-if="table.playerA && table.playerB" type="button" class="btn btn-primary w-30 float-end">
										Watch
									</button>
									<span v-else-if="!authenticated && !playButtonVisible(table)">Requires login to play</span>
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
				<div class="modal-body col-xs-12">
					
						<div class="" role="group" aria-label="Select game" id="select-game">
							<select v-model="selectedGame" @change="onSelectedGameChange" >
								<option :value="0" :key="0" >
									Select game
								</option>
								<option v-for="game in games" :value="game.gameId" :key="game.id">
									{{game.name}}
								</option>
							</select>
						</div>
						<div class=" ">
							<div v-if="hasVariants" class="" id="v-model-select-dynamic">	
								<select v-model="selectedGameMode" >
									<option :value="'0'" :key="0">
										Select variant
									</option>
									<option v-for="gameMode in gameModes" :value="gameMode.id" :key="gameMode.id">
										{{gameMode.name}}
									</option>
								</select>
							</div>
						</div>
							<div class="form-check " id="computer_selection">
								<input class="" type="checkbox" id="computerSelection" @change="onComputerSelectionChange" v-model="playAgainstComputerChecked">
								<label class="" for="computerSelection">Play against computer</label>
							</div>
							<div class="form-check " id="only_registered">
								<input class="" type="checkbox" id="onlyRegistered" v-model="onlyRegistered">
								<label class="" for="onlyRegistered">Play only against registered</label>
							</div>                          
							<div v-if="canRandomizeStarter" class="form-check " id="random_starter">
								<input class="" type="checkbox" id="randomStarter" v-model="randomStarterChecked">
								<label class="" for="randomStarter">Random starter</label>
							</div>
						
							<div v-if="isTimeControlledGame">
								Time control
								<div>				
									<select v-model="selectedTimeControl" >
										<option v-for="timeControl in getTimeControls()" :value="timeControl.id" :key="timeControl.id">
											{{timeControl.seconds}} seconds
										</option>
									</select>
								</div>
							</div>
                            <div v-if="canSelectPlayerAmount">
								Players
								<div>				
									<select v-model="selectedPlayerAmount" >
										<option v-for="count in getAllowedPlayerAmounts()" :value="count" :key="count">
											{{count}} players
										</option>
									</select>
								</div>
							</div>
				</div>
				<div class="modal-footer justify-content-between">
					<button type="button" class="btn btn-secondary " data-bs-dismiss="modal">Close</button>
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
import { IGameMode,IGame, IGameToken, ITable, IUser,ISquare, IChatMessage, IWinMessage,IWinTitle,IWin, IGameResult } from "../interfaces/interfaces";
import { loginMixin,GUEST } from "../mixins/mixins";
import { utilsMixin } from "../mixins/utilsmixin";
import { useRoute } from "vue-router";
import { Tooltip } from 'bootstrap/dist/js/bootstrap.esm.min.js'
//import * as bootstrap from 'bootstrap';

import Chat from "./Chat.vue";
import { tablesMixin } from "@/mixins/tablesMixin";
import { poolMixin } from "@/mixins/poolMixin";
export default defineComponent({
	
	name: "Lobby",
	mixins: [loginMixin, utilsMixin, tablesMixin, poolMixin],
	data() {
		return {		
			createTableButtonVisible:true,
			watchTableButtonDisabled:false,
			removeTableButtonVisible:false,
			selectedGame:0,
			selectedGameMode:"0",
			selectedTimeControl:0,
            selectedPlayerAmount:2,
			playAgainstComputerChecked:false,
			randomStarterChecked:false,
			onlyRegistered:false,
			computerLevel:"0",
            GUEST:GUEST
		}
	},
	computed:{
		
		users() {
			return this.$store.getters.users
		},
		tables(){
			return this.$store.getters.tables
		},
		games(){
			return this.$store.getters.games
		},
		gameModes(){
			if(this.selectedGame === 1){
				return this.$store.getters.games.filter(game => game.gameId === 1)[0].gameModes	
			}else if(this.selectedGame === 2){
				return this.$store.getters.games.filter(game => game.gameId === 2)[0].gameModes
			}else if(this.selectedGame === 3){
				
				return this.$store.getters.games.filter(game => game.gameId === 3)[0].gameModes
			}
		},
        hasVariants(){
            return this.selectedGame !== 0 && this.selectedGame !== 4
        },
		modalCreateTableDisabled(){			
			return this.selectedGameMode === "0" && this.selectedGame !== 4	
		},
		tablesExist(){
			return this.$store.getters.tables.length > 0
		},
		hasCreatedTable(){
			const firstTable:ITable = this.$store.getters.tables[0]
			if(firstTable && firstTable.playerA){
				return firstTable.playerA.name === this.userName
			}
			return false
		},
        canRandomizeStarter(){
            return this.selectedGame === 3 || this.selectedGame === 4
        },
        isTimeControlledGame(){
            //Pool = 3, Yatzy = 4
            return this.selectedGame === 3 || this.selectedGame === 4
        },
        canSelectPlayerAmount(){
            return this.selectedGame === 4
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
			let websocket = new WebSocket(import.meta.env.VITE_APP_WS_URL);

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
							this.$store.dispatch("setGames", data.games)
							this.$store.dispatch("setTables", data.tables)
						})	
						break;
					case "CREATE_TABLE":
						
						this.$store.dispatch("addTable", data.table)						
						if (data.table.playerA.name===this.userName){							
							this.createTableButtonVisible = false;
							this.removeTableButtonVisible = true
							
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
						this.$store.dispatch("startGame", data.table).then(() => {							
							const tableC:ITable = data.table
							if (tableC.playerA.name === this.userName || tableC.playerB.name === this.userName ){
								tableC.playerA.wins = 0
								tableC.playerB.wins = 0
								this.createTableButtonVisible = false
								this.removeTableButtonVisible = false
								this.watchTableButtonVisible = false
								this.$store.dispatch("selectTable", data.table).then(() => {
								this.openTable(data.table)
							})
							}
						})						
						break
					case "MOVE":
						const square :ISquare = {x: data.x, y: data.y, coordinates: data.x.toString().concat(data.y.toString()), token:data.token}					
						this.$store.dispatch("move", square)
						this.$store.dispatch("changeTurn", data.table.playerInTurn)
						break
					case "WATCH":
						console.log("watch arrived selecting table "+data.table)
						this.$store.dispatch("selectTable", data.table).then(() => {
							this.openWatcherTable(data.table)
						})
						break
					case "ADD_WATCHER":						
						this.$store.dispatch("addWatcher", data.who).then(() => {
							
						})		
						break		
					case "CHAT":
						const message:IChatMessage={text: data.message,from:data.from}
						if(data.to === "COMMON"){
							this.$store.dispatch("updateCommonChat", message)
							return
						}
						this.$store.dispatch("chat", message)
						break
					case "REMATCH":
						this.$store.dispatch("rematch", data.table).then(() => {
							//this.$router.push({ name: 'Table', id:data.table.id})
						})
						break
                    case "JOIN_TABLE":
                        console.log("Player joined to table but not starting yet "+JSON.stringify(data))
                        break;
					case "POOL_UPDATE":					
						this.$store.dispatch("poolUpdate", data)
						break
					case "POOL_SELECT_POCKET":
					
						this.$store.dispatch("poolSelectPocket", data)
						break
					case "POOL_HANDBALL":						
						if(data.pool.turnResult === "HANDBALL_FAIL"){
							
							this.$store.dispatch("poolSetHandBallFail", data)
						}else{	
							
							this.$store.dispatch("poolSetHandBall", data)
							const handBallMessage:IChatMessage = {
								from:data.from,
								text:"Handball set by " + data.table.playerInTurn.name
							}
							this.$store.dispatch("chat", handBallMessage);
						}
						break
					case "POOL_PLAY_TURN":
							
						this.$store.dispatch("poolPlayTurn", data).then(() => {
					
						})
						break
					case "LEAVE_TABLE":						
						this.$store.dispatch("leaveTable", data)
						break
					case "GAME_END":
							const lastSquare :ISquare = {x: data.x, y: data.y, coordinates: data.x.toString().concat(data.y.toString()), token:data.token}
							if(data.table.gameMode.gameNumber !== 3 ){
								//console.log("Game END:"+JSON.stringify(data))
								this.$store.dispatch("move", lastSquare)													
								if(data.gameResult.resultType === "DRAW"){
									const gameResult:IGameResult={table:data.table,win:data.win}								
									this.$store.dispatch("setDraw",gameResult);
								}
								else {
									const updateScoreMessage:IWinMessage =
									{
										winner:data.gameResult.winner.name,
										reason: IWinTitle.GAME,
										winsA:data.table.playerA.wins,
										winsB:data.table.playerB.wins,
										from:"System"
									}									
									this.$store.dispatch("updateScore", updateScoreMessage);
									const win:IWin = {
										fromX:data.gameResult.fromX,
										fromY:data.gameResult.fromY,
										toX:data.gameResult.toX,
										toY:data.gameResult.toY,
										resultType:data.gameResult.resultType,
										winner:{name:data.gameResult.winner.name},	
									}							
									this.$store.dispatch("updateWinner", win);
								}
								return
							} 
							if(data.table.gameMode.gameNumber === 3){
								this.$store.dispatch("poolPlayTurn", data)										
								this.$store.dispatch("poolGameEnded", data)
							}
						break
					case "RESIGN":
						const winMessage:IWinMessage={
							winner:data.who.name,
							reason:data.reason,
							winsA:data.table.playerA.wins,
							winsB:data.table.playerB.wins,
							from:data.from
						}
						this.$store.dispatch("resign", winMessage);
						const title: IWinTitle = data.message ==="R"?IWinTitle.RESIGNITION:IWinTitle.GAME
						let chatText = data.who.name.concat( title===IWinTitle.RESIGNITION? " won by resignation": " won")
               			const chatMessag:IChatMessage = {
							from:data.from,
							text:chatText
						}
						this.$store.dispatch("chat", chatMessag)
						break
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
			let obj = null
            const selectedGameMode = this.selectedGame === 4? 40: this.selectedGameMode
			obj = {title: "CREATE_TABLE", message: selectedGameMode , computer:this.playAgainstComputerChecked, randomStarter:this.randomStarterChecked, onlyRegistered:this.onlyRegistered}
		
		 	if(this.isTimeControlledGame){ 
				let index = this.selectedTimeControl
				if(!index)
					index = 0
				obj.timeControlIndex = index
			}
            if(this.canSelectPlayerAmount){
				obj.playerAmount = this.selectedPlayerAmount
			}
            console.log("DATA:"+JSON.stringify(obj))
			this.user.webSocket.send(JSON.stringify(obj))
			this.computerLevel = null
			this.playAgainstComputerChecked = false
			this.randomStarterChecked = false
			this.selectedTimeControl = null
		},
		getTableShortDesc(gameMode:number){
			if(gameMode < 20){
				return "x.o"
			}else if(gameMode < 30){
				return "4x"
			}
            else if(gameMode < 40){
				return "Pool"
			}
            return "Yatzy"
		},
      
		getBoardDesc(table:ITable){
			if(table.gameMode.gameNumber < 3){
				return table.x +" x "+ table.y
			}else if(table.gameMode.gameNumber === 3){
				return "8-ball"
			}
            else if(table.gameMode.gameNumber === 4){
				return table.playerAmount +"p"
			}
			return "err"
		},
		onComputerSelectionChange(){
			if(!this.playAgainstComputerChecked){				
				this.computerLevel="0";
			}
		},		
		onSelectedGameChange(event){
			if(event.target.selectedIndex === 3){ // pool			
				this.selectedGameMode = 30///8-ball is the only choice
				return
			}
			this.selectedGameMode = "0"
		},
		removeTable(){
			
			const obj = { title: "REMOVE_TABLE",message:""};
			this.user.webSocket.send(JSON.stringify(obj));
		},
		play(table:ITable){
			
			const obj = { title: "JOIN_TABLE", message:table.tableId};
			this.user.webSocket.send(JSON.stringify(obj));
		},
		playButtonVisible(table:ITable){
			if(table.playerA && table.playerB){
				return false				
			}
			if(table.registeredOnly && !this.authenticated)	{
				return false
			}
			return true	
		},
		isOwnTable(table:ITable){
			return	table?.playerA?.name === this.userName
		},
		watchTable(table:ITable){			
			const obj = { title: "WATCH",message:table.tableId};
			this.user.webSocket.send(JSON.stringify(obj));
		},
		openProfile(selectedName:string){
			if(!selectedName.startsWith(GUEST)){
				this.$router.push({ name: 'Profile', params: { selectedName: selectedName } })	
			}
		},	
		getTimeControl(table:ITable){
			return this.getTimeControls()[table.timeControlIndex].seconds
		},
        getAllowedPlayerAmounts(){
			return [2, 3, 4]
		}
	}
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style>
.games-profile-link{
	cursor:pointer
}


</style>
