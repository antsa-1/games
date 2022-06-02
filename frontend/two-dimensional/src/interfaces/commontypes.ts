import { IPlayer } from "./interfaces"
//no game specific typings here
export interface IBaseTable {
    playerA: IPlayer,
    playerB: IPlayer,
    playerInTurn: IPlayer,
    chat: IChat,
    tableId: string,
    timeControlIndex: number,
    secondsLeft: number,
    randomStarter: boolean,
    registeredOnly: boolean,
    playerAmount: number,
    gameMode: IGameMode,
    tableType: string,
    started: boolean
}
export interface IMultiplayerTable extends IBaseTable {
    players: Array<IPlayer>
}

export interface IChat {
    messages: IChatMessage[];
    message: IChatMessage;
}
export interface IGameMode {
    gameId: number,
    gameNumber: number,
    id: number,
    name: string,
}
export interface IChatMessage {
    from?: string;
    text: string;
}

export interface IVector2 {
    x: number,
    y: number,
}

export interface IGameCanvas {
    canvas: HTMLCanvasElement
    animating: boolean
}

export interface IGameOptions {
    notificationSound: boolean
}

export interface Image {
    image: HTMLImageElement,
    canvasDimension: IVector2,
    canvasDestination: IVector2,
    canvasRotationAngle: IVector2,
    realDimension: IVector2,
    visible: boolean
}

export interface ITurn {
    turnResult?: ITurnResult,
    nextTurnPlayer?: IPlayer,
    changePlayer?: boolean,
    lastTurn?: boolean,
    result?: ITurnResult,
    player?: IPlayer, // string
}

export interface ITurnQueue {
    turns: Array<ITurn>[],
    blocked: boolean
}

export interface ITurnResult {
    winner?: IPlayer,
    text: string,
    draw?: boolean
}
