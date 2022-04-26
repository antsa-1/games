<template>
	<div class="row">
		<div class="col-xs-12 col-sm-12">
			<span class="fw-bold"> Eight ball briefly: </span>
			<p>
			1. Starting player sets the handball on the left side of the line by clicking the position with mouse.<br>
			2. At first the table is open => players choose either solids or stripes to play with.<br>
			3. Aim with mouse, put cursor near the ball you want to hit. <br> Holding mouse button down gives force to the cue, until maxed out or releasing the button.<br>
			4. Last ball to put into a pocket is eight ball. A pocket selection must be done before pocketing.<br>
			5. Player wins if eight ball is hit first and it goes to the selected pocket while cue ball stays on the table.<br>
			6. Other than starting handball can be placed freely at the table unless it is overlapping other ball.
			</p>
			<p>
				<span class="fw-bold"> Mobile clients </span>
				It is possible to play the games also with mobile client.
				<p>
					Canvas, where the games are playd, is not responsive. However it is possible to zoom in and out.
					Playing can be easier if device supports horizontal layout by turning the device.<br>
					EightBall:
					Mobile device uses browser's pointer events which are not supported by all devices.	<br>
					Both aiming and cue force loading happens by holding finger (or similar) on the screen at wanted point. <br>
					Zooming in and out _should_ be also possible before aiming has started.
				</p>
			</p>
		</div>	
	</div>	
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { loginMixin } from "../mixins/mixins";
import { IUser } from "../interfaces/interfaces";
export default defineComponent({
	name: "Instructions",
	mixins: [loginMixin],

	data() {
	
	},
	computed: {
	
	},

	created() {
		
	},

	methods: {
		
		
	}
});
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<!-- @author antsa-1 from GitHub -->
<style scoped>
</style>
