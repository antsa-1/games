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
    started: boolean,
}
export interface IMultiplayerTable extends IBaseTable {
    players: Array<IPlayer>
    canvasEnabled: boolean
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
    element: HTMLCanvasElement
    animating: boolean,
    ctx: CanvasRenderingContext2D
}

export interface IGameOptions {
    notificationSound: boolean
}

export interface Image {
    image: HTMLImageElement,
    canvasDimension: IVector2,
    canvasDestination: IVector2,
    canvasRotationAngle: number,
    realDimension: IVector2,
    visible: boolean, 
}

export interface IAction {
    turnResult?: IActionResult,
    nextTurnPlayer?: IPlayer,
    changePlayer?: boolean,
    lastTurn?: boolean,
    result?: IActionResult,
    player?: IPlayer, // string
}

export interface IActionQueue {
    actions: Array<IAction>[],
    blocked: boolean
}

export interface IActionResult {
    winner?: IPlayer,
    text: string,
    draw?: boolean
}

export enum FONT_SIZE {
    DEFAULT = "15px",
    LARGER = "16px",
    LARGEST = "18px"
}

export enum FONT {
    DEFAULT = "Arial"
}