import { ITable, IPlayer } from "@/interfaces/interfaces"
import { IVector2, Image, IActionResult, IGameOptions } from "@/interfaces/commonTypes"
import { IAction as ICommonTurn } from "@/interfaces/commonTypes"

export interface IPoolComponent {
    position: IVector2,
    image: Image,
    velocity: IVector2,
}
export interface IEightBallGame {
    canvas: HTMLCanvasElement,
    balls: Array<IBall>[],
    resultSnapshot: any, //action
    cueBall: IBall,
    cue: ICue,
    poolTable: IPoolTable,
    gameOptions: IEightBallGameOptions,
    mouseCoordsTemp: IVector2,
    handBall: boolean,
    pocketSelection: boolean,
    selectedPocket: IPocket,
    turnQueue: ITurnQueue,
    playingTurn: boolean,

}

export interface ITurn extends ICommonTurn {
    cue?: ICue,
    cueBall?: IBall,
    setSelectedPocket?: number,
    setHandBall?: boolean,
    askHandBallPosition?: boolean,
    askPocketSelection?: boolean,
    shootBall?: boolean,
    winner?: string,
    winReason?: string,
    whoPlayed: string
}

// extending ITable is dubious -> inherits properties which are not related to this pooltable. But helps to keep only one table and avoiding type checks
export interface IPoolTable extends IPoolComponent, ITable {
    pointerLine: IPointerLine,
    mousePoint: IVector2,
    mouseEnabled: boolean,
    topLeftPart: IBoundry,
    topRightPart: IBoundry,
    rightPart: IBoundry,
    bottomRightPart: IBoundry,
    bottomLeftPart: IBoundry,
    leftPart: IBoundry,
    pockets: Array<IPocket>[],
}

export interface IBoundry {
    a: number,
    b: number,
    c: number
}

export interface IPocket {
    center: IVector2,
    radius: number,
    pathwayRight: IPathWayBorder,
    pathwayLeft: IPathWayBorder,
}
export interface IPathWayBorder {
    top: IVector2,
    bottom: IVector2
}
export interface IBall extends IPoolComponent {
    diameter: number,
    radius: number,
    number: number,
    color: string, // Yellow or Red if numbers are not displayed
    inPocket: boolean
}
export interface ICue extends IPoolComponent {
    force: number,
    angle: number
}
export interface IPointerLine {
    startPosition: IVector2,
    endPosition: IVector2,
}

export interface IEightBallGameOptions extends IGameOptions {
    pointerLine: boolean,
}