Validátor pro NDK

Použité verze:
 
Apache Maven 3.2.3   # poslední stabilní verze
Java Development Kit 1.7.0 update 71   # poslední stabilní verze řady 7
 
Postup sestavení:
 
# export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"   # navýšení oproti imlicitním hodnotám
 
# mvn install -DskipTests   # překlad/instalace prerekvizit (trasformačního modulu NDK)
 
# cd validation
# mvn install -DskipTests   # překlad/instalace vlastního validátoru
