import { ITable, IGame, IUser, IPlayer, IStoreState, ISquare, IGameResult, IWinMessage, IWin, } from "./interfaces/interfaces";
import {  IChatMessage, IChat,IBaseTable,IMultiplayerTable } from './interfaces/commontypes';

import { createStore } from 'vuex';
import { IYatzyTable } from "./interfaces/yatzy";

export const store = createStore<IStoreState>({
    state: {
        user: null,
        games: [],
        tables: [],
        users: [],
        theTable: null,
        commonChat: {
            messages: [],
            message: null
        },
        loadingStatus: false
    },
    getters: {
        user(state):IUser {
            if (state.user) {
                return state.user
            }
            const userName = sessionStorage.getItem("userName")
            const token = sessionStorage.getItem("token")
            if (userName && token) {

                const user: IUser = { "name": userName, "token": token }
                store.dispatch("setUser", user);
                return state.user
            }
            return null
        },
        loading(state) {
            return state.loadingStatus
        },
        userName(state) {
            if (state.user) {
                return state.user.name
            }
            return null
        },
        users(state) {
            return state.users
        },
        tables(state) {
            return state.tables
        },
        games(state) {
            return state.games
        },
        theTable(state) {
            return state.theTable
        },
        chatMessages(state) {
            if (state.theTable && state.theTable.chat) {
                return state.theTable.chat.messages
            }
            return []
        },
        commonChatMessages(state) {
            return state.commonChat.messages
        },
       
        loadingStatus(state) {
            return state.loadingStatus
        },
        playerInTurn(state){  
            return state.theTable.playerInTurn
        }
    },

    mutations: {
        setUser(state, user: IUser) {
            if (user) {

                sessionStorage.setItem("userName", user.name)
                sessionStorage.setItem("token", user.token)
                state.user = user

            } else {

                sessionStorage.removeItem("userName")
                sessionStorage.removeItem("token")
                state.user = user
            }
        },
        updateUserTableId(state, tableId:string){
            state.user.tableId = tableId
        },
        setGames(state, games: IGame[]) {
            state.games = games
        },
        joinMultiplayerTable(state, multitable:IMultiplayerTable){           
           let ytable:IMultiplayerTable = <IMultiplayerTable> state.tables.find(table => table.tableId === multitable.tableId )  
           ytable.players = multitable.players
        },
         setTables(state, tables: ITable[]) {

            state.tables = tables
        },
        clearTable(state) {           
            state.theTable = null           
        },
        setUsers(state, players: IUser[]) {
            state.users = players
        },
        updateScore(state, winMessage: IWinMessage) {
            if (state.theTable) {
                state.theTable.playerA.wins = winMessage.winsA
                state.theTable.playerB.wins = winMessage.winsB
                state.theTable.playerInTurn = null
            }
        },
        addTable(state, table: ITable) {
            if (table.playerA.name === state.user.name) {
                state.tables.unshift(table)
            }
            else {
                state.tables.push(table);
            }
        },
        removeFromPlayerList(state, user: IUser) {

            state.users = state.users.filter((listUser) => {
                return listUser.name !== user.name;
            })

        },        
        removeTable(state, data: ITable) {
            state.tables = state.tables.filter((table) => {

                return table.tableId !== data.tableId;
            })
        },
        addPlayer(state, user: IUser) {

            state.users.push(user)
        },
        startGame(state, table: IBaseTable) {
            const index = state.tables.findIndex(element => element.tableId === table.tableId)          
            state.tables.splice(index, 1, table)
        },
        selectTable(state, table: ITable) {
            let chat: IChat = { messages: [], message: { text: "" } }
            table.chat = chat
            const board = table.board
            table.board = board
            state.theTable = table;
        },
        addWatcher(state, player: IPlayer) {
            if (state.theTable) {
                let text = player.name.concat(" joined to watch")
                const chatMessage: IChatMessage = { from: "System", text: text }
                state.theTable.chat.messages.unshift(chatMessage)
            }
        },
        move(state, square: ISquare) {            
          let table:ITable = <ITable> state.theTable
          table.board.push(square)
        },
        changeTurn(state, playerInTurn: IPlayer) {            
            state.theTable = { ...state.theTable, playerInTurn: playerInTurn }
        },
        chat(state, message: IChatMessage) {
            if (state.theTable) {
                state.theTable.chat.messages.unshift(message);
            }
        },
        updateCommonChat(state, message: IChatMessage) {

            state.commonChat.messages.unshift(message);
        },
        resign(state, winMessage: IWinMessage) {

            if (state.theTable) {
                state.theTable.playerA.wins = winMessage.winsA
                state.theTable.playerB.wins = winMessage.winsB
                state.theTable.playerInTurn = null
            }
        },
        leaveStartedTable(state, userName: string) {
            if (state.theTable) {
                if (state.theTable.playerA.name === userName) {
                    state.theTable.playerInTurn = null
                } else if (state.theTable.playerB.name === userName) {
                    state.theTable.playerInTurn = null

                }               
                let text = userName.concat(" left table")
                const chatMessage: IChatMessage = { from: "System", text: text }
                state.theTable.chat.messages.unshift(chatMessage)
            }
        },
        leaveStartingTable(state, data:any) {            
            let tableFrom:IMultiplayerTable = data.table
            let userName:string = data.who.name
            let table:IMultiplayerTable = <IMultiplayerTable>state.tables.find(table => table.tableId == tableFrom.tableId)
            if(table && table.players){
                let index:number = table.players.findIndex(player => player.name === userName)
                table.players.splice(index, 1)
            }
        },
        rematch(state, table: ITable) {
            let chat: IChat = { messages: [], message: { text: "" } };
            if (state.theTable && state.theTable.chat) {
                chat = state.theTable.chat
            }
          //  const board: ISquare[] = []
           // table.board = board
            const rematchMessage: IChatMessage = { text: "Rematch started", from: "System" }
            chat.messages.unshift(rematchMessage)
            table.playerA.wins = table.playerA.wins
            table.playerB.wins = table.playerB.wins
            table.chat = chat
            state.theTable = table
        },
        updateWinner(state, win: IWin) {

            if (this.state.theTable) {
                state.theTable.playerInTurn = null
                const chatMessage: IChatMessage = { text: win.winner.name.concat(" won"), from: "System" }
                state.theTable.chat.messages.unshift(chatMessage)
                let table:ITable = <ITable> state.theTable
                table.win = win               
            }
        },
        setDraw(state, gameResult: IGameResult) {

            if (this.state.theTable) {
                if (state.theTable.playerA) {
                    state.theTable.playerA.draws = gameResult.table.playerA.draws
                }
                if (state.theTable.playerB) {
                    state.theTable.playerB.draws = gameResult.table.playerB.draws
                }
                if (state.theTable.playerInTurn) {
                    state.theTable.playerInTurn = null
                }
                const chatMessage: IChatMessage = { text: "Game ended in draw", from: "System" }
                state.theTable.chat.messages.unshift(chatMessage)
            }
        },
        setLoadingStatus(state, loading: boolean) {
            state.loadingStatus = loading
        },

    },
    actions: {
        poolUpdate(context, object) {
            // PoolTable has subscribed to this action
        },
        poolPlayTurn(context, object) {
            // PoolTable has subscribed to this action
        },
        poolSetHandBall(context,object){
            // PoolTable has subscribed to this action
        }, 
        poolSelectPocket(context,object){
            // PoolTable has subscribed to this action
        }, 
        poolSetHandBallFail(context,object){
            // PoolTable has subscribed to this action
        },        
        poolGameEnded(context, object){
            
        } ,
        yatzyRollDices(context, object) {
           
        },
        setUser(context, user: IUser) {
            context.commit('setUser', user)

        },
        setGames(context, games: string[]) {

            context.commit('setGames', games)
        },
        setTables(context, tables: ITable[]) {

            context.commit('setTables', tables)
        },
        setUsers(context, players: IPlayer[]) {

            context.commit('setUsers', players)
        },
        addTable(context, table: ITable) {

            context.commit('addTable', table)
        },
        removePlayer(context, user: IUser) {
            context.commit('removeFromPlayerList', user)

        },
        removeTable(context, table: ITable) {

            context.commit('removeTable', table)

        },
        addPlayer(context, user: IUser) {

            context.commit('addPlayer', user)

        },
        startGame(context, table: IBaseTable) {

            context.commit('startGame', table)

        },
        selectTable(context, table: ITable) {
            context.commit('selectTable', table)
        },
        addWatcher(context, user: IPlayer) {
            context.commit('addWatcher', user)
        },
        move(context, square: ISquare) {
            context.commit('move', square)
        },
        changeTurn(context, playerInTurn: IPlayer) {
            context.commit('changeTurn', playerInTurn)
        },
        chat(context, message: IChatMessage) {
            context.commit('chat', message)
        },
        updateCommonChat(context, message: IChatMessage) {
            context.commit('updateCommonChat', message)
        },
        resign(context, message: IWinMessage) {
            context.commit('resign', message)
        },
        updateScore(context, message: IWinMessage) {
            context.commit('updateScore', message)
        },
       
        rematch(context, table: ITable) {
            context.commit("rematch", table)
        },
        clearTable(context) {
            context.commit("clearTable")
        },
        leaveTable(context, data) {         
            if(!data?.table?.started){
                context.commit("leaveStartingTable", data)
            }
            else if(!context.state.theTable){               
               return
            }
            else if(context.state.theTable.tableId === data.table.tableId)
                context.commit("leaveStartedTable", data.who.name)
        },
        updateWinner(context, win: IWin) {
            context.commit("updateWinner", win)
        },
        setDraw(context, gameResult: IGameResult) {

            context.commit("setDraw", gameResult)
        },
        joinMultiplayerTable(context, table:IMultiplayerTable){
            context.commit("joinMultiplayerTable", table) 
        },
        updateUserTableId(context, tableId:string){
          
            context.commit("updateUserTableId", tableId)  
        },
        logout(context, user: IUser) {

            context.commit("setTables", [])
            context.commit("setUsers", [])
            if (user && user.webSocket) {

                user.webSocket.close();
                user.webSocket = null
            }
            const requestOptions = {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: null
                },
                body: JSON.stringify({
                    token: user?.token
                })
            }
            const apiURL = import.meta.env.VITE_APP_API_BASE_URL + "/portal/api/user/logout"
            context.commit('setUser', null)
            fetch(apiURL, requestOptions).then(response => {
            })
        },
        setLoadingStatus(context, loading: boolean) {
            context.commit("setLoadingStatus", loading)
        }
    }
})

/*
 * 	@author antsa-1 from GitHub 
*/