<template>

	User profile of <b>{{userName}}</b>
	<br>

	TBD Own profile component vs existing User-component. <br>Use case 1: Edit own profile and settings <br> UC2 show public profile pages/game statistics of current user
	<br>
	Use case 3 direct URLs to profiles vs. VUE internal state.
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { loginMixin } from "../mixins/mixins";
import { IUser } from "../interfaces";
export default defineComponent({
	name: "Profile",
	mixins: [loginMixin],
	props: {
		userName: String
	},
	data() {
		return {
		
		};
	},
	computed: {
	
	},

	created() {
		console.log("Public profile of :"+this.userName)
	},

	methods: {
		
		
		},
	
});
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
