import { IUser, GameResult } from "@/interfaces/interfaces";


export const utilsMixin = {
	data() {
		return {
			token: null
		};
	},
	computed: {


	},
	created() {


	},
	methods: {
		convertInstantToDate(instant: string) {
			return new Date(instant).toLocaleDateString()
		},
		convertInstantToTime(instant: string) {
			return new Date(instant).toLocaleTimeString()
		}, convertResultToText(result: number) {
			const val = GameResult[result]
			return val.toLowerCase()
		},
		getRankingChange(a: number, b: number) {
			if(a && b){				
				const sign= b >= a ? "-" : "+"
				const amount=Math.abs(a-b)				
				const result=sign.concat(amount.toFixed())
				return result
			}
		},
		fixVal(val:number){
			return val.toFixed()
		},	
	},
};