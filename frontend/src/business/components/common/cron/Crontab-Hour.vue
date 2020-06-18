<template>
	<el-form size="small">
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
				小时，允许的通配符[, - * /]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				周期从
				<el-input-number v-model='cycle01' :min="0" :max="60" /> -
				<el-input-number v-model='cycle02' :min="0" :max="60" /> 小时
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
				从
				<el-input-number v-model='average01' :min="0" :max="60" /> 小时开始，每
				<el-input-number v-model='average02' :min="0" :max="60" /> 小时执行一次
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				指定
				<el-select clearable v-model="checkboxList" placeholder="可多选" multiple style="width:100%">
					<el-option v-for="item in 60" :key="item" :value="item-1">{{item-1}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>
	</el-form>
</template>

<script>
export default {
	data() {
		return {
			radioValue: 1,
			cycle01: 0,
			cycle02: 1,
			average01: 0,
			average02: 1,
			checkboxList: [],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-hour',
	props: ['check', 'cron'],
	methods: {
		// 单选按钮值变化时
		radioChange() {
			if (this.radioValue === 1) {
				this.$emit('update', 'hour', '*', 'hour');
				this.$emit('update', 'day', '*', 'hour');
			} else {
				if (this.cron.min === '*') {
					this.$emit('update', 'min', '0', 'hour');
				}
				if (this.cron.second === '*') {
					this.$emit('update', 'second', '0', 'hour');
				}
			}
			switch (this.radioValue) {
				case 2:
					this.$emit('update', 'hour', this.cycle01 + '-' + this.cycle02);
					break;
				case 3:
					this.$emit('update', 'hour', this.average01 + '/' + this.average02);
					break;
				case 4:
					this.$emit('update', 'hour', this.checkboxString);
					break;
			}
		},
		// 周期两个值变化时
		cycleChange() {
			if (this.radioValue == '2') {
				this.$emit('update', 'hour', this.cycleTotal);
			}
		},
		// 平均两个值变化时
		averageChange() {
			if (this.radioValue == '3') {
				this.$emit('update', 'hour', this.averageTotal);
			}
		},
		// checkbox值变化时
		checkboxChange() {
			if (this.radioValue == '4') {
				this.$emit('update', 'hour', this.checkboxString);
			}
		}
	},
	watch: {
		"radioValue": "radioChange",
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'checkboxString': 'checkboxChange'
	},
	computed: {
		// 计算两个周期值
		cycleTotal: function () {
			this.cycle01 = this.checkNum(this.cycle01, 0, 23)
			this.cycle02 = this.checkNum(this.cycle02, 0, 23)
			return this.cycle01 + '-' + this.cycle02;
		},
		// 计算平均用到的值
		averageTotal: function () {
			this.average01 = this.checkNum(this.average01, 0, 23)
			this.average02 = this.checkNum(this.average02, 1, 23)
			return this.average01 + '/' + this.average02;
		},
		// 计算勾选的checkbox值合集
		checkboxString: function () {
			let str = this.checkboxList.join();
			return str == '' ? '*' : str;
		}
	}
}
</script>