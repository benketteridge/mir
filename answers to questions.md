1\. How long did you spend on the coding test? What would you add to your solution if you spent more time on it? If you didn't spend much time on the coding test then use this as an opportunity to explain what you would add.
> The coding exercise took about 12 hours to get the code to the state you can see in the Github repo. Obviously, a couple of hours of that time was spent getting started with Ratpack, and a couple more with Redis, neither of which I've ever used before. If I'd taken more time, I would:
> * spend more time with Redis to see if there's a more efficient way to store a collection of transactions against a hash 
> * rebuild request routing to do it more in a more purely ratpack-y way, rather than pragmatically getting it to work by replicating what I know from Spring. 
> * parameterised the set up of the Jedis connection pool,
> * added more logging,
> * checked the security model clears up properly after handling every request,
> * prevent negative Spend calls from increasing the balance.
>
> However, the deployment part of the exercise proved a bit of a disaster. I could get the application deployed to a pod, and I could get a redis instance deployed to a pod.
> I could connect the application on my development machine at home to the redis instance, and that worked normally.
> I could connect to the deployed application with a browser or Postman, and get 'Unauthorised' errors from the GET /balance API that indicated I was connecting to a successfully deployed application.
> But I just could not get the deployed application to connect to the redis instance.  

2\. What was the most useful feature that was added to Java 8?
Please include a snippet of code that shows how you've used it.
> I wouldn't say that there are any truly essential parts of Java 8 - most of the changes are either syntactic sugar (lambdas, foreach), extensions to existing libraries (some of the Collections changes), or bringing an existing library into the mainstream Java family (joda time -> java.time). None of them really gave me a feeling of "oh, I _wish_ we'd had this before!"
>
> However, the first thing that was apparent was that the removal of the PermGen memory space made using Tomcat in development a lot less painful.
>
> In more recent times, I've used default methods on JpaRepository interfaces, but always felt slightly guilty for doing so, as an interface shouldn't really contain real code in Java.
```java
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    @Cacheable("allCurrencies")
    default Currency getByCode(String code) {
        return findById(code).orElseGet(null);
    }
}
```
>This has the effect of applying the @Cacheable annotation to the getByCode() implementation that Spring Data Jpa auto-generates on start-up.

3\. What is your favourite framework / library / package that you love but couldn't use in the task?
What do you like about it so much?
> Spring Data Jpa is fantastic if you you're writing JPA query classes (usually known as Jpa DAOs). In Spring, injectable beans are almost always written as an interface, and a default implementation class.
> Prior to the availability of Spring Data Jpa, DAO implementations were full of pretty simple calls to a JPA EntityManager with predictable patterns.
> Whilst these DAOs are much considerably better than the old ResultSet-based JDBC versions, with little risk of resource leakage (for example), they still contain plenty of boiler-plate.
> Spring Data Jpa on the other had, auto generates the DAO implementation at runtime from the interface (known as a Repository), but you still get all the CRUD operations in the DAO. 
> (the CurrencyRepository defined above has various find/get, insert, update, and delete methods with no effort, and you can define custom methods using a DSL that do all sorts of complex fetches)
>
> My favourite library that I _could_ use, by the way, is lombok. It auto-generates default implementations of getter and setters, toString, equals and hashcode methods, and different types of constructors.
> This removes _so much_ boiler plate code from POJOs in particular, but is also useful in simplifying Spring DI classes (if you use the now-recommended constructor injection, rather than property injection).
> IntelliJ IDEA and Eclipse both have plugins that enable lombok to be used in real time.

4\. What great new thing you learnt about in the past year and what are you looking forward to learn more about over the next year?
> IntelliJ IDEA has been a revelation after using Eclipse for more years than I care to recall. Combining Spring Boot and IntelliJ IDEA made my development code-launch-debug cycle considerably faster, as Spring Boot 
> applications start in seconds under these conditions, just like RatPack.
> As for the next year, I don't know what I'll be needing to learn yet. I had Python on my list for learning very soon, but this is on hold for the time being. A number of open source tools I use for type-setting music 
> are written using Python, and that's why I was interested in learning it.

5\. How would you track down a performance issue in production?
Have you ever had to do this? 
Can you add anything to your implementation to help with this?
> It depends what you mean by performance issue. The issues I've seen that spring to mind resulted from misusing remote resources, in particular, databases.
> Considerations for approaching a performance issue:
> 1. is it limited to one environment? (can you reproduce it in development?)
> 1. is it limited to one use case, or is it systemic?  
> 1. in a web UI, is it clear that the problem is in the client or the server?
> 1. can you see any one particular resource that is a bottleneck?
> 
> The ideal being that you can see that it's one particular query on the database, and that you reproduce the behaviour in dev where you have all your normal debug/trace tools to hand.
> In the applications I've built and supported, I'd then move on to logging the SQL queries and seeing if any take a long time (DBA access to the database could short-circuit this if the DB is logging all SQL).
> Has the use of the application changed from the design expectations? Is there a missing index? Does the query have an unexpected cartesian join? Has a table the query is using grown beyond the design expectations? Was the developer lazy and used a view designed for one job (single row fetch) for a different one (mass retrieval)? (yes, guilty)
> 
>As for my implementation of the test API, my main performance worry would be around the Spend API. The others are relatively trivial. The Spend API requires all the existing transactions for the hash to be loaded and parsed and then serialized at the end. This _could_ grow to be a problem.
>decent request logging (perhaps only if stages in the API call take longer than 0.5s) could help isolate problematic requests and accounts.
>The API is designed to be performant, but only within my understanding of Redis. It may be that the transactions list would have to be partitioned at a certain length, or each year's worth of transactions could be reduced to a net-change transaction after some time (if the loss of detail is acceptable, after 12 months).  

6\. How would you improve the APIs that you just used?
>1. add a way to set the initial balance to a different amount than $100
>1. add a legitimate way to increase the balance, (other than sending a negative Spend, which would increase the balance!)
>1. add rules for what to do with different currencies (exchange rates to be stored in the datastore, perhaps?)
>1. add rules for handling a negative balance (is it even allowed? can accounts have an authorised overspend limit?)
>1. add a call-back to an authentication service to be sure that the login is from a legitimate user
>1. add rules for monitoring patterns of spending, providing alerts of particularly high transaction rates, for example.
>1. remove the 'date' field from the Spend API, as that should be 'now' (or record the 'now' as well as the specified 'date')
>1. add administrative access to see all the balances, reset accounts, block accounts

7\. Please describe yourself in JSON format.
```json
    {
        "forename": "Ben",
        "surname": "Ketteridge",
        "origin": "Cambridge, UK",
        "education": "PhD",
        "married": "true",
        "parent": "true",
        "geek": "true",
        "hobbies": [
            {"hobbyType": "music", "instrument": "violin"}, 
            {"hobbyType": "music", "instrument": "bass guitar"},
            {"hobbyType": "reading", "genre": "sci-fi"},
            {"hobbyType": "reading", "genre": "fantasy"},
            {"hobbyType": "reading", "genre": "science non-fiction"},
            {"hobbyType": "gaming", "genre": "RPG"},
            {"hobbyType": "gaming", "genre": "action/adventure"}
        ],
        "fruit" : "grape",
        "colour": "rich purple"
    }
```

8\. What is the meaning of life?

    42 (but, if the answer is 42, what was the question?)