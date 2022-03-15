export interface IPoolComponent {
    position: IVector2,
    image: IGameImage,
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
}
export interface IBall extends IPoolComponent {

    diameter: number,
    radius: number;
    velocity: IVector2,
    number: number,
    color: string, // Yellow or Red if numbers are not displayed
}
export interface ICue extends IPoolComponent {

    //  angle: number,
    force: IVector2,
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