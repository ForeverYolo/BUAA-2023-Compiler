.data
	global:
	.space 4
	str_0: .asciiz "Hello World\n"
.text
	la $28,global
	# InitGlobal
		# Decl Global_0
	# main_func
		main_func:
		addiu $29,$29,-12
		# PutStr Const_0
			la $4,str_0
			addiu $2,$0,4
			syscall
		# Scan Temp_0
			addiu $2,$0,5
			syscall
			addu $5,$2,$0
		# Add Global_0 Temp_0 Const_0
			addiu $16,$5,0
		# PutNumber Global_0
			addu $4,$16,$0
			addiu $2,$0,1
			syscall
		# Add Temp_1 Const_0 Const_0
			addiu $6,$0,0
		# Return Temp_1
			addiu $2,$0,10
			syscall
