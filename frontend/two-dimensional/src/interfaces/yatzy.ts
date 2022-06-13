import { ITable, IPlayer, } from "@/interfaces/interfaces"
import { Image, IGameCanvas, IBaseTable, IMultiplayerTable, IVector2, IAction, IActionResult } from "@/interfaces/commonTypes"

export interface IYatzyGame {
    snapshot: any, //action
}
export interface IYatzySnapshot {
    table: IYatzyTable,
    yatzy: IYatzyMessage
}

export interface IYatzyMessage {
    dices: IDice[],
    whoPlayed: string
}

export interface IYatzyActionQueue {
    actions: IYatzyAction[],
    blocked: boolean
}

export interface IYatzyAction {
    actionName: string,
    rollsLeftAfterAction: number,
    rollResult?: IDice[],
    options?: IOption[],
    whoPlayed?: string,
    nextTurnPlayer: IPlayer,
}

export interface IYatzyTable extends IMultiplayerTable, IYatzyComponent {
    players: IYatzyPlayer[],
    canvas: IGameCanvas,
    dices: IDice[],
    playButton: IButton
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
    scoreCard: IScoreCard,
    rollsLeft: number
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

export interface IScoreCard extends IYatzyComponent {
    hands: IHand[],
    subTotal: number,
    total: number,
    bonus: number
}
export interface IHand {
    handType: HandType,
    value: number
}
export enum IOption {
    ROLL_DICES,
    SELECT_HAND,

}
export enum HandType {
    PAIR = 1, TWO_PAIR, TRIPS, QUADS, YATZY, SMALL_STRAIGHT, LARGE_STRAIGHT, ONES, TWOS, THREES, FOURS, FIVES, SIXES, FULL_HOUSE, CHANCE
}