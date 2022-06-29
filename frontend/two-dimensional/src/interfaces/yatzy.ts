import { ITable, IPlayer, } from "@/interfaces/interfaces"
import { Image, IGameCanvas, IBaseTable, IMultiplayerTable, IVector2, IAction, IGameOptions } from "@/interfaces/commonTypes"

export interface IYatzyGame {
    snapshot: any, //action
}
export interface IYatzySnapshot {
    table: IYatzyTable,
    yatzy: IYatzyMessage
}

export interface IYatzyMessage {
    dices: IDice[],
    whoPlayed: string,
    lastPlayedScoreCard: IScoreCard,
    gameOver: boolean
}

export interface IYatzyActionQueue {
    actions: IYatzyAction[],
    blocked: boolean
}

export interface IYatzyAction {
    type: string,
    payload: any,
}

export interface IYatzyTable extends IMultiplayerTable, IYatzyComponent {
    players: IYatzyPlayer[],
    canvas: IGameCanvas,
    dices: IDice[],
    playButton: IButton,
    scoreCardRows: IScoreCardRow[],
    secondsLeft: number,
    gameOver: boolean
}

export interface IButton extends IYatzyComponent {
    image: Image,
    text?: string
}

export interface ISection {
    start: IVector2,
    end: IVector2
}

export interface IYatzyPlayer extends IPlayer {
    rollsLeft: number,
    scoreCard: IScoreCard,
    enabled: boolean
}

export interface IYatzyComponent {
    position: IVector2,
    image?: Image,
}
export interface IDice extends IYatzyComponent {
    number: number,
    selected: boolean,
    position: IVector2,
    diceId: string
}

export interface IScoreCard {
    subTotal: number,
    total: number,
    bonus: number,
    hands: IHand[],
    lastAdded: IHand
}

export interface IScoreCardRow {
    section: ISection,
    title: string,
    nth: number,
    handType: HandType,
    height: number
}
export interface IHand {
    handType: HandType,
    typeNumber: number
    value: number
}
export enum IOption {
    ROLL_DICES,
    SELECT_HAND,

}
export enum HandType {
    ONES = 1, TWOS, THREES, FOURS, FIVES, SIXES, PAIR, TWO_PAIR, TRIPS, FULL_HOUSE, SMALL_STRAIGHT, LARGE_STRAIGHT, QUADS, CHANCE, YATZY, BONUS, SUBTOTAL, TOTAL

}