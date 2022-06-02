import { ITable, IPlayer, } from "@/interfaces/interfaces"
import { IVector2, IGameCanvas, IBaseTable, IMultiplayerTable } from "@/interfaces/commonTypes"

export interface IYatzyGame {
    snapshot: any, //action
}

export interface IYatzyTable extends IMultiplayerTable {
    players: Array<IYatzyPlayer>,
    canvas: IGameCanvas,
}

export interface IYatzyPlayer extends IPlayer {
    dices: Array<IDice>,
    scoreCard: IScoreCard,
}

export interface IDice {
    face: number,
    locked: boolean
}

export interface IScoreCard {
    hands: IHand,
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