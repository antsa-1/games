import { ITable, IPlayer, } from "@/interfaces/interfaces"
import { Image, IGameCanvas, IBaseTable, IMultiplayerTable, IVector2 } from "@/interfaces/commonTypes"

export interface IYatzyGame {
    snapshot: any, //action
}

export interface IYatzyTable extends IMultiplayerTable, IYatzyComponent {
    players: IYatzyPlayer[],
    canvas: IGameCanvas,
}

export interface IYatzyPlayer extends IPlayer {
    dices: IDice[],
    scoreCard: IScoreCard,
}

export interface IYatzyComponent {
    position: IVector2,
    image?: Image,
}
export interface IDice extends IYatzyComponent {
    face: number,
    locked: boolean,
    position: IVector2
}

export interface IScoreCard extends IYatzyComponent  {
    hands: IHand[],
    subTotal: number,
    total: number,
    bonus: number
}
export interface IHand {
    handType: HandType,
    value: number
}

export enum HandType {
    PAIR = 1, TWO_PAIR, TRIPS, QUADS, YATZY, SMALL_STRAIGHT, LARGE_STRAIGHT, ONES, TWOS, THREES, FOURS, FIVES, SIXES, FULL_HOUSE, CHANCE
}