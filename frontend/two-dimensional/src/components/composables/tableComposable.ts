import { store } from "@/store"

export function leaveTable(tableId: string) {
    const obj = { title: "LEAVE_TABLE", message: tableId }
    store.getters.user?.webSocket?.send(JSON.stringify(obj))
    store.dispatch("clearTable")
}

export function rematch() {
    const obj = { title: "REMATCH", message: "" }
    store.getters.user?.webSocket.send(JSON.stringify(obj))
}

export function isMyName(name: string) {
    store.getters.userName === name
}