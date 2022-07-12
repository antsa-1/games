import { IResultPlayer, GameResult } from "@/interfaces/interfaces"

export function convertInstantToDate(instant: string) {
    return new Date(instant).toLocaleDateString()
}

export function convertInstantToTime(instant: string) {
    return new Date(instant).toLocaleTimeString()
}

export function getRankingChange(player: IResultPlayer): string {
    if (player.initialRanking >= 0 && player.finalRanking >= 0) {
        const sign = player.finalRanking >= player.initialRanking ? "+" : "-"
        const amount = Math.abs(player.initialRanking - player.finalRanking)
        const result = sign.concat(amount.toFixed())
        return result
    }
}

export function fixVal(val: number) {
    return val?.toFixed()
}

export function convertGameResultToText(result: number) {
    const val = GameResult[result]
    return val.toLowerCase()
}