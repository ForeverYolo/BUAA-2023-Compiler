int x[10],i = 0;
int main()
{
	for(;i <= 10;)
	{
	    printf("i = %d\n",i);
		if (i == 2)
		{
			i = 4;
			continue;

		}
		x[i] = i;
		i = i + 1;
		if (i == 5) break;
	}
	return 0;
}
