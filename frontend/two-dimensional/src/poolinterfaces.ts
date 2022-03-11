export interface IPoolTable {
    canvas: HTMLCanvasElement,
    poolTableImage: HTMLImageElement,
    ballsRemaining: Array<IBall>[],
    cueBall: IBall,
    cue: ICue,
    pointerLine: IPointerLine,
    mousePoint: IPosition,
 
}
export interface IBall {
    diameter: number,
    position: IPosition,
    number: number,
    color: string, // Yellow or Red if numbers are not displayed
    image: HTMLImageElement
}
export interface ICue {
    position: IPosition,
    image: HTMLImageElement,
    updateRotation(): number,
    angle: number,
    width: number,
    height: number,
    
}
export interface IPointerLine {
    startPosition: IPosition,
    endPosition: IPosition,
}

export interface IPosition {
    x: number,
    y: number
}
