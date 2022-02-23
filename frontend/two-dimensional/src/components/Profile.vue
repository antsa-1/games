<template>

	User profile of <b>{{userName}}</b>
	Member since {{memberSince}}
	<br>
	Profile:
	{{text}}
<br>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { loginMixin } from "../mixins/mixins";
import { IProfile, IUser } from "../interfaces";
export default defineComponent({
	name: "Profile",
	mixins: [loginMixin],
	props: {
		userName: String
	},
	data():IProfile{
		return {
		stats: null,
		text: null,
		memberSince:null
		};
	},
	computed: {
	
	},

	created() {
		console.log("Public profile of :"+this.userName)
		this.fetchProfile()
	},

	methods: {
		fetchProfile() {
			console.log("Ui starts fetch")			
			this.errorFlag = false	
			const requestOptions = {
				method: "GET",
				headers: {
					"Content-Type": "application/json",
					Authorization: null
				},
			
			}
			const apiURL=process.env.VUE_APP_API_BASE_URL+"/portal/api/profile/"+this.userName
			this.$store.dispatch("setLoadingStatus", true)
			fetch(apiURL, requestOptions)
				.then(response => {					
					if (response.status !== 200) {
						return Promise.reject("error fetching profile");
					}
					return response.json();
				}).then(
					data => {						
						console.log("PROFILE:"+JSON.stringify(data))
						this.text=data.profileText
						this.memberSince= new Date(data.memberSince).toLocaleString()
					},
					(err) => {
						this.errorFlag = true			
					}
				).finally(() => {				
					this.$store.dispatch("setLoadingStatus", false)
				});
		},
		
		},
	
});
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
