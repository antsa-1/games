export interface IStoreState {
	user: IUser,
	gameModes: IGameMode[],
	tables: ITable[],
	users: IUser[],
	commonChat: IChat,
	theTable: ITable,
	loadingStatus?: boolean
}

export enum IGameToken {
	O = "O",
	X = "X",
}
export enum IWinTitle {
	GAME = "G",
	RESIGNITION = "R",
}
export interface IWinMessage {
	winner: IPlayer;
	reason: IWinTitle;
	winsA: number,
	winsB: number,
	from: string,
}
export interface IWin {
	fromX: number,
	fromY: number,
	toX: number,
	toY: number,
	winner: IPlayer,
}

export interface IGameResult {
	win: IWin,
	table: ITable

}
export interface ISquare {
	coordinates: string,
	x: number,
	y: number,
	token?: IGameToken
}
export interface IWinSquares {
	square: ISquare[]
}
export interface IGameMode {
	gameId: number,
	id: number,
	name: string,
	requiredConnections: number,
	gameNumber: number,
}
export interface IChat {
	users?: IUser;
	messages: IChatMessage[];
	message: IChatMessage;
}
export interface IChatMessage {
	from?: string;
	text: string;
}

export interface LoginData {
	userName: string;
	password: string;
	loginToken: string;
	loginError: boolean;
}

export interface Lobby {
	lobbyists: string[];
}

export interface ITable {
	playerA: IPlayer;
	playerB: IPlayer;
	playerInTurn: IPlayer;
	gameMode: IGameMode;
	board: ISquare[];
	win?: IWin;
	id: string;
	chat: IChat;
	x: number;
	y: number;
}

export interface IPlayer {
	name: string,
	gameToken?: IGameToken, //GameToken in a table
	wins?: number; // GameSession wins in a table
	draws?: number;// GameSession draws in a table
}

export interface IUser {
	webSocket?: WebSocket;
	game?: ITable;
	name: string;
	token?: string;
	email?: string;
}

export interface ITopLists {
	connectFours: IPlayerStats[],
	tictactoes: IPlayerStats[],
	errorFlag: boolean,
	dateTime: string,
	totalTictactoes: number,
	totalConnectFours: number,
}

export interface IPlayerStats {
	name: string,
	playedConnectFours: number,
	playedTicTacToesAgainstAI: number,
	playedConnectFoursAgainstAI: number,
	rankingConnectFour: number,
	rankingTicTacToe: number,
}

export interface IProfile {
	stats: IPlayerStats,
	text: string,
}

export interface ISettings {
	newPlayerSound: boolean,
	newTicTacToeTableSound: boolean,
	newConnectFourTableSound: boolean
}
