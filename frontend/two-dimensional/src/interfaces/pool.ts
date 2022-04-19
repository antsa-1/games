import { IBaseTable, ITable, IPlayer } from "@/interfaces/interfaces"

export interface IPoolComponent {
    position: IVector2,
    image: IGameImage,
    velocity: IVector2,
}
export interface IEightBallGame {
    canvas: HTMLCanvasElement,
    ballsRemaining: Array<IBall>[],
    resultPlayerABalls: Array<IBall>[],
    resultPlayerBBalls: Array<IBall>[],
    resultRemainingBalls: Array<IBall>[],
    resultCueBallPosition: IVector2
    cueBall: IBall,
    cue: ICue,
    poolTable: IPoolTable,
    gameOptions: IEightBallGameOptions,
    mouseCoordsTemp: IVector2,
    handBall: boolean,
    pocketSelection: boolean,
    selectedPocket: IPocket,
    turnQueue: Array<ITurn>[]
}

export interface ITurn {
    cue?: ICue,
    cueBall?: IBall,
    setSelectedPocket?: number,
    setHandBall?: boolean,
    turnResult?: string,
    askHandBallPosition?: boolean,
    askPocketSelection?: boolean,
    nextTurnPlayer?: IPlayer
    shootBall?: boolean
}
export interface ITurnResult {
    text: string
}

export interface IGameImage {
    image: HTMLImageElement,
    canvasDimension: IVector2,
    canvasDestination: IVector2,
    canvasRotationAngle: IVector2,
    realDimension: IVector2,
    visible: boolean
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
export interface IVector2 {
    x: number,
    y: number,
}

export interface IEightBallGameOptions {
    helperOrigo: boolean,
    helperLine: boolean,
    useAnimation: boolean
}