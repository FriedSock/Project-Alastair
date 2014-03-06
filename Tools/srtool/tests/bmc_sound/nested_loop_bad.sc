void main()
{
	int i;
	int j;
	i=0;
	j=0;
	while(i < 2)
	{
		i = i + 1;
        while(j < 2)
        {
            j = j;
        }
	}

    assert(i == 2);
	assert(j == 2);

}

