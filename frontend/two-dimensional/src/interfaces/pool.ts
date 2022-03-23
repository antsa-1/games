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
    mouseEnabled: boolean,
    topLeftPart: IBoundry,
    topRightPart: IBoundry,
    rightPart: IBoundry,
    bottomRightPart: IBoundry,
    bottomLeftPart: IBoundry,
    leftPart: IBoundry,
    topLeftPocket: IPocket,
    topMiddlePocket: IPocket,
    topRightPocket: IPocket,
    bottomRightPocket: IPocket,
    bottomMiddlePocket: IPocket,
    bottomLeftPocket: IPocket
}

export interface IBoundry {
    a: number,
    b: number,
    c: number
}

export interface IPocket {
    center: IVector2,
    radius: number
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