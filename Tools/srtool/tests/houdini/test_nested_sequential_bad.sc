

void main()
{
 int i;
 int iterations;
 int j;
 j=0;
 i=0;

 // make iterations positive, but otherwise unconstrained
 assume(iterations > 0);

 while(i != 50 ) {

     while(i < iterations)
      cand(i <= iterations)
      cand(i == 74)
      cand(i != -1)
     {
      i = i + 1;
     }

     assert(i == iterations);

     while(i > 0)
     {
       i = i - 1;
     }
 }

 assert(j == 0);
 assert(i == 0);

}

