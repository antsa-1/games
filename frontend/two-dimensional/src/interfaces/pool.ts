export interface IPoolComponent {
    position: IVector2,
    image: IGameImage,
    moving: boolean,
    velocity: IVector2,
}
export interface IEightBallGame {
    canvas: HTMLCanvasElement,
    ballsRemaining: Array<IBall>[],
    cueBall: IBall,
    cue: ICue,
    poolTable: IPoolTable,
    gameOptions: IEightBallGameOptions
}

export interface IGameImage {
    image: HTMLImageElement,
    canvasDimension: IVector2,
    canvasDestination: IVector2,
    canvasRotationAngle: IVector2,
    realDimension: IVector2,
    visible: boolean
}

export interface IPoolTable extends IPoolComponent {
    pointerLine: IPointerLine,
    mousePoint: IVector2,
    boundries: IPoolTableBoundries
    mouseEnabled: boolean,
    pockets: IPockets
}

export interface IPoolTableBoundries {
    top: number,
    right: number,
    bottom: number,
    left: number,
}

export interface IPockets {
    topLeft: IPocket,
    topMiddle: IPocket,
    topRight: IPocket,
    bottomRight: IPocket,
    bottomMiddle: IPocket,
    bottomLeft: IPocket
}
export interface IPocket {
    middle: IVector2,
    direction?:number
}
export interface IBall extends IPoolComponent {
    diameter: number,
    radius: number,
    number: number,
    color: string, // Yellow or Red if numbers are not displayed
    collided: boolean
}
export interface ICue extends IPoolComponent {
    force: number,
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
    helperLine: boolean
}