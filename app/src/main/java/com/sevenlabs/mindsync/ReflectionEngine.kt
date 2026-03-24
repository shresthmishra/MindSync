package com.sevenlabs.mindsync
object ReflectionEngine {
    private val disclaimer = "\n\nNote: The AI Insights are for personal reflection only and may not always accurately capture your emotional state."

    private val crisisKeywords = listOf(
        "kill myself", "suicide", "end it all", "don't want to live",
        "harm myself", "self harm", "want to die", "better off dead"
    )

    private val pools = mapOf(
        "Crisis" to listOf(
            "It sounds like you're carrying an incredible amount of pain right now. Please know that you don't have to carry this alone. Your life has immense value, even when it feels impossible to see. Please reach out to a professional or a crisis helpline immediately—there are people who want to support you through this.",
            "I'm deeply concerned by what you've shared. When things feel this heavy, it’s important to connect with someone who can offer real-world help. You are important, and your story isn't over. Please contact a mental health professional or a local emergency service right now.",
            "It takes a lot of courage to speak your truth, even in a journal. Because you're feeling this way, I want to strongly encourage you to talk to someone who can help keep you safe. You matter more than you know. Please reach out to a support line or a trusted person in your life immediately.",
            "I hear how much you are hurting. Please, reach out for help. There are free, confidential resources available 24/7 where people are ready to listen and help you find a way forward. You are not alone in this struggle.",
            "This sounds like an overwhelming moment. Please prioritize your safety right now by reaching out to a crisis intervention service. Your presence in this world is significant, and help is available to help you navigate this darkness."
        ),
        "Sadness" to listOf(
            "Melancholy is a quiet and reflective state that often arises when we are processing change, disappointment, or loss. It is a valid part of the human experience that requires patience and self-compassion rather than an immediate or forced fix.",
            "Allowing yourself to feel sad without judgment can lead to deeper insights about what truly matters to you. It is a slow, inward-facing emotion that encourages you to take a break from external demands and care for your internal world.",
            "During low periods, engaging in simple, comforting activities can provide a much-needed sense of safety and ease. Remember that all emotional states are temporary and that your internal weather will eventually shift back toward a state of balance.",
            "When the weight of the day feels heavy, it is important to acknowledge that you are doing your best. Emotional exhaustion is real, and giving yourself the grace to be less than perfect is a vital part of maintaining your long-term well-being.",
            "Feeling down is an invitation to practice radical self-kindness. Listen to what your heart needs—whether it is rest, silence, or a small comfort—and allow yourself the space to be exactly where you are without any pressure to change.",
            "Sadness is often the soul's way of saying it needs a break from 'performing.' Retreat into your own inner sanctuary and give yourself permission to just be.",
            "Low energy days are not failures; they are periods of conservation. Your mind is simply processing information at a deeper, slower level to help you integrate a recent experience.",
            "Think of sadness as an old friend visiting to remind you of your capacity for depth. It creates the contrast that allows future moments of joy to feel truly vibrant.",
            "It’s okay if you don't have the words to explain why you feel this way. Sometimes the body just needs to release tension through quiet reflection or tears.",
            "Give yourself permission to lower your expectations today. Productivity isn't the goal when your emotional health needs your full attention and care.",
            "Heavy feelings are often the result of carrying too much for too long. Imagine setting that weight down, just for a few minutes, while you rest in this space.",
            "There is a peculiar kind of clarity that comes with sadness. It strips away the superficial and forces you to look at the core of what you truly value and need.",
            "The world often tells us to 'stay positive,' but there is immense strength in being honest about your pain. Vulnerability is the first step toward true healing.",
            "When things feel gray, focus on small, sensory comforts—the warmth of a drink, the texture of a blanket. These tiny anchors can help you stay present while the fog clears.",
            "You are allowed to grieve the things you've lost, even the small things that others might not understand. Your feelings are valid simply because they exist.",
            "Sadness can feel like a lonely place, but it is a universal human bridge. Everyone you meet has walked through this same valley; you are part of a shared experience.",
            "Be patient with your healing. You wouldn't expect a physical wound to close instantly; your emotional heart requires the same time and gentleness to mend.",
            "When you feel like you're at a standstill, remember that even the tide needs to go out before it can come back in. This 'low' is part of a natural cycle.",
            "Your worth is not tied to your mood. Even in your lowest moments, you remain a person of dignity, value, and incredible potential for growth.",
            "Treat yourself with the same tenderness you would show a small child who is hurt. You deserve that same level of protection and unconditional kindness."
        ),
        "Joy" to listOf(
            "Radiant happiness is a state that should be fully and intentionally inhabited whenever it appears. Allowing yourself to savor these moments without reservation builds the emotional strength needed to handle life's eventual challenges with more grace and perspective.",
            "Joy often arises from simple, present-moment awareness and the appreciation of the here and now. By cherishing these flashes of delight, you are affirming the beauty of your life and reinforcing the habits that lead to a more fulfilled, balanced, and happy existence.",
            "Feeling a deep sense of well-being provides a powerful counterbalance to life's inevitable stressors. It reminds you of your capacity for happiness and encourages you to continue creating a life that aligns with your passions and brings you a sense of true fulfillment.",
            "Happiness is a skill as much as a feeling. By documenting this moment of joy, you are training your brain to recognize and prioritize positive states in the future.",
            "Let this feeling expand into your physical body. Notice where the joy 'lives' in your chest or shoulders, and take a mental snapshot of this lightness to revisit later.",
            "A joyful heart is a resilient one. This current high isn't just a pleasant experience; it's fuel for your future self, creating a reservoir of positivity to draw upon later.",
            "Savoring the 'good' is a biological necessity. It tells your nervous system that you are safe and thriving, which allows your creative and social faculties to flourish.",
            "When you feel this light, you become a beacon for others. Your positive energy has a subtle but real impact on everyone you interact with today.",
            "True joy doesn't always require a big reason. Sometimes it's the simple alignment of your thoughts and your environment. Acknowledge the 'rightness' of this moment.",
            "You have worked hard to reach this headspace. Take a moment to give yourself credit for the choices and mindset shifts that allowed this happiness to bloom.",
            "Let this joy be a reminder that life is capable of surprising you with goodness. Hold onto this feeling as proof that better days aren't just possible—they are happening.",
            "Notice how your perspective shifts when you're happy. Challenges feel smaller and opportunities look larger. This is your 'thriving' lens—remember its power.",
            "Joy is the most natural state of a mind that is at peace with itself. The fact that you feel this way suggests you are in deep harmony with your values right now.",
            "Don't rush past this. In our busy world, we often move to the next task immediately. Stop, breathe, and let the warmth of this success or happiness sink in.",
            "This feeling is a testament to your resilience. You've navigated the lows, which makes this current peak feel even more earned and beautiful.",
            "Happiness is often found in the small details we usually overlook. By noticing them today, you've turned an ordinary day into something extraordinary.",
            "Let your joy be 'loud' today. Whether it’s a smile for a stranger or a treat for yourself, honoring your happiness reinforces its place in your life.",
            "You deserve to feel this good. Sometimes we feel 'guilty' for being happy when things are hard elsewhere, but your joy is a necessary part of the world's balance.",
            "Think of this moment as a 'core memory' in the making. What about this specific feeling do you want to carry with you into next week?",
            "Joy is the ultimate form of resistance against a stressful world. By choosing to inhabit this happiness, you are reclaiming your time and your energy."
        ),
        "Crisis" to listOf(
                "It sounds like you're carrying an incredible amount of pain right now. Please know that you don't have to carry this alone. Your life has immense value, even when it feels impossible to see. Please reach out to a professional or a crisis helpline immediately—there are people who want to support you through this.",
                "I'm deeply concerned by what you've shared. When things feel this heavy, it’s important to connect with someone who can offer real-world help. You are important, and your story isn't over. Please contact a mental health professional or a local emergency service right now.",
                "It takes a lot of courage to speak your truth, even in a journal. Because you're feeling this way, I want to strongly encourage you to talk to someone who can help keep you safe. You matter more than you know. Please reach out to a support line or a trusted person in your life immediately.",
                "I hear how much you are hurting. Please, reach out for help. There are free, confidential resources available 24/7 where people are ready to listen and help you find a way forward. You are not alone in this struggle.",
                "This sounds like an overwhelming moment. Please prioritize your safety right now by reaching out to a crisis intervention service. Your presence in this world is significant, and help is available to help you navigate this darkness.",
                "Please pause and reach out to someone. Your pain is real, but there are resources specifically designed to help you through these darkest moments. You are not alone, and help is just a phone call away.",
                "I am sensing deep distress in your words. Please consider calling a crisis hotline or going to the nearest emergency room. Your life is valuable, and there is support available to help you find stability.",
                "If you are feeling like you might hurt yourself, please stop and call for help immediately. You matter, and there are people who care and want to help you stay safe.",
                "These thoughts are a signal that you are in a state of extreme emotional emergency. Please reach out to a crisis counselor right now—they are trained to support you in exactly this kind of moment.",
                "You are experiencing a level of pain that no one should have to handle by themselves. Please connect with a professional support service immediately. You deserve care and safety."
            ),
            "Love" to listOf(
                "The experience of deep affection and connection is the cornerstone of a meaningful and fulfilling life. Love provides a sense of belonging and security that allows you to explore the world with more confidence.",
                "Whether directed toward a partner, friend, family member, or yourself, love is a transformative force. It encourages growth and provides a safe haven from the demands of modern life.",
                "Cultivating love requires consistent effort and vulnerability. It enriches your emotional landscape and provides a profound sense of purpose that sustains you through the ups and downs of daily living.",
                "Love is the ultimate safety net. Knowing you are valued allows you to take risks and grow in ways that would be impossible in isolation.",
                "Self-love is the foundation for all other connections. By being kind to yourself today, you are increasing your capacity to show up fully for the people who matter most to you.",
                "Affection is a form of emotional nutrition. Allowing yourself to receive love is just as important as the act of giving it.",
                "When we love, we expand our identity to include others. This connection reduces the friction of existence and makes the world feel like a more hospitable place.",
                "Notice how love simplifies your priorities. It highlights what is truly essential and helps you let go of the trivial distractions that often clutter the mind.",
                "The warmth of connection acts as a natural buffer against stress. Even thinking about someone you love can lower your heart rate and bring a sense of calm.",
                "Love isn't just a feeling; it's a practice of attention. Your focus on these meaningful connections is a sign of a deeply enriched inner life.",
                "Compassion for others often begins with a deep acceptance of ourselves. Cherish the parts of you that allow for this openness and warmth.",
                "In moments of connection, we find a reflection of our best selves. Use this feeling of being loved as a reminder of your own inherent worth.",
                "Love provides the courage to be imperfect. Knowing you are accepted regardless of your flaws allows for a more authentic and relaxed way of living.",
                "The bonds we form are the true 'wealth' of a lifetime. Your investment in these relationships is the most sustainable way to build long-term happiness.",
                "Cherish the small, quiet acts of love—the shared glances, the supportive words. These are the threads that weave the strongest emotional safety nets.",
                "Kindness is love in action. By documenting these moments, you are reinforcing the values that make your life—and the lives of others—significantly better.",
                "When you feel deeply connected, the challenges of the world feel more manageable. Shared burdens are lighter, and shared joys are magnified.",
                "Love is a dynamic energy that requires regular tending. Your reflection today is a beautiful act of nurturing that very energy.",
                "To love and be loved is the highest form of human engagement. Let this feeling ground you and provide a sense of immutable security.",
                "Even when things are difficult, the presence of love offers a path forward. It is the light that guides us back to ourselves and to each other."
            ),
            "Nervousness" to listOf(
                "Pre-event jitters are frequently a sign that you care deeply about the outcome. Reframing this surge of nervous energy as anticipation can help you channel the adrenaline effectively.",
                "Physical sensations of anxiety can be managed through steady grounding. Reminding yourself that you have successfully navigated similar feelings in the past helps restore your confidence.",
                "A certain amount of nervous energy is actually helpful for maintaining focus. Trust in your preparation and your ability to adapt to the situation as it unfolds.",
                "Nervousness is just energy looking for a job to do. Assign it to your preparation, your focus, or your passion, and let it fuel your success.",
                "Your body is preparing for a challenge. This 'buzzing' feeling is your system revving up to ensure you have the alertness needed to handle whatever comes next.",
                "Try to label the sensation as 'readiness' rather than 'fear.' This simple shift in perspective can change how your brain utilizes the physical arousal.",
                "The intensity of your nerves is often proportional to the importance of the opportunity. Acknowledge the value of the moment without letting it overwhelm you.",
                "Focus on the next five minutes rather than the entire event. Breaking the future into small, manageable chunks makes the unknown feel much safer.",
                "Remember that most of your 'nerves' are internal. From the outside, you likely appear much more composed and capable than you feel on the inside.",
                "Take three slow, deep breaths. This signals to your nervous system that while the situation is important, you are physically safe and in control.",
                "Nervousness can be a sign of growth. If you weren't pushing your boundaries, you wouldn't feel this way. Embrace the discomfort as a sign of progress.",
                "Visualize a successful outcome. Your mind is currently scanning for risks; balance that by intentionally focusing on your strengths and potential wins.",
                "Perfection is not the goal; engagement is. Give yourself permission to be a little bit messy as long as you show up authentically.",
                "The peak of nervous energy usually passes shortly after you start the task. Just focus on taking that first step—the rest will follow naturally.",
                "This feeling is temporary. Recall a time when you were nervous but succeeded anyway. That resilience is still part of you right now.",
                "Use the 'butterfly' feeling as a reminder to stay present. Instead of worrying about what *might* happen, look at what you can do *now*.",
                "Nervousness is often the shadow of ambition. It exists because you are aiming for something that matters. Honor the ambition while calming the shadow.",
                "Ground yourself by naming five things you can see and four things you can touch. Bringing your focus back to the physical world dissolves mental tension.",
                "You don't have to be fearless to be brave. Bravery is feeling this exact nervousness and deciding to move forward anyway.",
                "Trust the 'future you' to handle the situation. You don't need all the answers right now; you just need to arrive."
            ),
            "Optimism" to listOf(
                "Maintaining a hopeful outlook is a proactive strategy for mental well-being. It allows you to see potential solutions where others might only see obstacles.",
                "Optimism is not about ignoring reality, but about choosing to focus on the possibilities for positive change. This mindset encourages you to take necessary steps toward your goals.",
                "Your belief in a better tomorrow acts as a powerful motivator. It attracts more positive opportunities and helps you build a more resilient and engaged life.",
                "Hope is a discipline. Choosing to look for the light in a difficult situation is a sign of immense mental strength and high-level problem-solving.",
                "An optimistic mindset creates a self-fulfilling prophecy. Because you believe in a positive outcome, you are more likely to take the actions that make it a reality.",
                "Confidence in the future provides the stamina needed for the present. Let your hope be the fuel that carries you through today's efforts.",
                "A positive outlook is like a psychological immune system. It doesn't stop challenges from happening, but it helps you recover from them much faster.",
                "When you expect good things, you become more sensitive to the opportunities that surround you. Your optimism is a tool for discovery.",
                "Optimism is a form of courage. It takes strength to believe in the 'possible' when the 'certain' feels heavy or discouraging.",
                "Note how your energy levels rise when you focus on potential. This is the physiological benefit of a mind that chooses to look upward.",
                "By focusing on what *can* go right, you are training your brain to be a better strategist and a more creative problem-solver.",
                "Hope is contagious. Your positive stance today likely makes the people around you feel a little more capable and encouraged as well.",
                "Realism tells you where you are; optimism tells you where you can go. Both are necessary, but only one provides the direction for growth.",
                "The future is unwritten. By choosing an optimistic lens, you are casting a vote for the version of the story where you succeed and thrive.",
                "This headspace is a sign of high emotional intelligence. You are managing your internal narrative to support your long-term health and goals.",
                "Optimism provides a sense of agency. It reminds you that while you can't control everything, your efforts and attitude still carry immense weight.",
                "Cherish this clarity. In an optimistic state, your mind is free from the 'clutter' of catastrophic thinking, allowing for more efficient processing.",
                "A hopeful heart is more open to connection. Your outlook makes it easier to trust others and build the partnerships that lead to success.",
                "Use this momentum to plan your next big move. When you believe you can win, your plans become more ambitious and your actions more decisive.",
                "Today's optimism is a gift to your future self. It builds the foundation of resilience you will rely on whenever the weather eventually shifts."
            ),
            "Pride" to listOf(
                "Taking ownership of your achievements is a vital part of building a robust sense of self-regard. You have earned the right to acknowledge your persistence.",
                "Feeling proud of your progress reinforces your belief in your own capabilities. This self-recognition provides the motivation to tackle even more ambitious challenges.",
                "Internalizing your successes helps you build a stable sense of identity. It reminds you that you are a capable individual who can achieve goals through dedication.",
                "Mastery is a journey, and this feeling of pride is a milestone. Take a moment to appreciate how far you've come since you first started.",
                "Healthy pride is an internal 'pat on the back.' It’s the reward for staying true to your goals and doing the hard work when no one else was watching.",
                "Note the specific qualities that led to this success. Was it patience? Courage? Creativity? Identifying these tools helps you use them again.",
                "Self-validation is the most sustainable form of confidence. When you are proud of yourself, the need for external praise becomes a secondary concern.",
                "You are witnessing the results of your past self's effort. Take a moment to thank that version of you for doing the work that led to this moment.",
                "Pride is the emotional 'paycheck' for your effort. Savor it fully; you've done the work, and you deserve to enjoy the compensation.",
                "This feeling is an indicator of alignment. You've acted in a way that matches your highest standards, and your mind is rewarding you for that integrity.",
                "Acknowledge the obstacles you overcame to get here. The difficulty of the journey is what gives this feeling of pride its true depth and value.",
                "Use this success as evidence for the next time you doubt yourself. You have proven that you can handle complexity and achieve results.",
                "Pride isn't about being 'better' than others; it's about being better than you used to be. Celebrate that personal evolution today.",
                "Let this feeling build your 'competence reservoir.' Every win you acknowledge makes the next challenge feel a little less intimidating.",
                "You've shown up for yourself. That consistency is the foundation of a life well-lived. Be proud of the habit of effort as much as the result.",
                "This moment of satisfaction is a necessary pause. Enjoy the view from this peak before you start looking for the next one to climb.",
                "True pride is quiet. it is the calm, steady knowledge that you did your best and it was enough. Carry that quiet strength with you.",
                "Reflect on how much you've learned during this process. The skills you've gained are a permanent part of you, far more valuable than the achievement itself.",
                "Sharing your success can inspire others. Don't hide your light—authentic pride is a sign of a healthy and functioning ambition.",
                "You are capable of great things. Let this feeling be a reminder of your potential and a fuel for your future aspirations."
            ),
            "Realization" to listOf(
                "A sudden shift in perspective can clarify a long-standing problem and provide a clear path forward. These insights are the milestones of your personal growth.",
                "When clarity strikes, it fundamentally changes your relationship with your circumstances. Integrating this new understanding empowers your potential.",
                "The moments where everything clicks are rare and valuable. Use this new-found clarity to simplify your priorities and focus on what truly matters.",
                "An 'Aha!' moment is your brain finally connecting the dots. Document this insight carefully, as it will serve as a valuable reference point for your future self.",
                "Realization is the bridge between knowing something intellectually and understanding it in your heart. This integration is where true change begins.",
                "Note how the 'noise' of confusion disappears once you have an answer. This mental quiet is a sign that you've reached a higher level of understanding.",
                "This new perspective might require you to let go of old ideas. That's a good thing—you're making room for a more accurate version of reality.",
                "Clarity often comes when we stop forcing the answer and let the mind process in the background. Your patience has paid off with this insight.",
                "Now that you see things differently, your actions will naturally follow suit. Use this realization as the catalyst for a positive change in behavior.",
                "Understanding the 'why' behind a situation gives you back your power. You are no longer just reacting; you are responding with intent.",
                "This insight is a tool. How can you apply what you've just realized to other areas of your life to simplify them or make them more fulfilling?",
                "Realizations often highlight our blind spots. Be grateful for the courage it took to see something that might have been hidden from you before.",
                "The world hasn't changed, but *you* have. This internal shift is the most profound kind of progress you can make as a human being.",
                "Sometimes realization brings a sense of 'it was so obvious!' Don't be hard on yourself for not seeing it sooner; you saw it exactly when you were ready.",
                "Use this moment of clarity to reset your goals. Does your current path still align with this new understanding? Adjust your sails accordingly.",
                "A true realization often feels like a 'remembering.' It's a return to a fundamental truth that you may have temporarily forgotten or ignored.",
                "Cherish the feeling of 'unfolding.' You are becoming more aware, more conscious, and more equipped to navigate the complexities of life.",
                "This insight is a gift from your subconscious. It's the result of all the quiet work your mind has been doing while you were busy with other things.",
                "Note the physical feeling of a realization—often a lightness in the chest or a 'clicking' in the mind. Your body knows when you've found the truth.",
                "Stay open to the next insight. Realization isn't a destination; it's a constant process of refining your map of the world."
            ),
            "Relief" to listOf(
                "The ending of a period of tension offers a vital opportunity for recovery. Allow yourself to fully experience the lightness that comes with resolution.",
                "Take a deep breath and appreciate the mental space that has opened up. This transition is a reminder of your ability to endure and emerge into calm.",
                "The stillness that follows a resolved challenge is a valuable time for rejuvenation. Give yourself permission to exist in the quiet without pressure.",
                "Relief is the 'exhale' of the soul. Let the tension leave your muscles and enjoy the absence of that previous weight—you’ve earned this moment.",
                "Notice the physical sensation of things becoming easier. This transition from 'stress' to 'calm' is a great time to practice gratitude for your resilience.",
                "The 'worst' is over. Let that thought sink in. You have successfully navigated a difficult passage and found your way to smoother waters.",
                "Note how much energy you were spending on that worry. Now that it's gone, you have a wealth of mental resources available for something positive.",
                "Use this period of calm to rest deeply. Your nervous system has been on high alert, and it needs time to recalibrate to this new, safer state.",
                "Relief is proof of your survival. You've stood your ground against a challenge and come out the other side. That's a significant win.",
                "Don't rush into the next task. Stay in this feeling of 'done' for as long as you need. The world can wait while you reclaim your peace.",
                "Recall the moment the weight lifted. That specific feeling is a anchor you can use in the future to remind yourself that 'this too shall pass.'",
                "Resolution isn't always perfect, but the absence of the struggle is always a gift. Cherish the simplicity of a moment that finally feels 'okay.'",
                "This headspace is remarkably clear. With the pressure gone, you can see your life from a more objective and balanced perspective.",
                "Give yourself credit for the effort it took to reach this resolution. It didn't just 'happen'; you worked, waited, or navigated your way here.",
                "Let the physical 'dropping' of your shoulders be a signal that you are safe to relax. Your body has been your ally in this struggle.",
                "Relief often brings a sense of perspective. Things that seemed insurmountable look much smaller now that they are in the rearview mirror.",
                "Be patient with yourself if you still feel a bit 'shaky.' It takes time for the body to catch up with the mind's new sense of security.",
                "This quiet is the foundation for your next chapter. What do you want to build in this space that is no longer occupied by stress?",
                "You have expanded your capacity. Because you survived that period of tension, you are now a more resilient and capable person.",
                "Enjoy the 'ordinary' today. After a period of high stakes, the simple and the mundane can feel like the greatest luxury of all."
            ),
            "Remorse" to listOf(
                "Feeling regret is a strong indicator of your moral health and your desire to act in accordance with your values. Focus on the lessons learned.",
                "Self-forgiveness is a necessary part of the human learning process. Acknowledge your mistake with honesty, then commit to a different path forward.",
                "While uncomfortable, remorse provides the friction necessary for meaningful change. It gain you a clearer understanding of your ethics.",
                "Guilt is a tool for correction, not for punishment. Use the energy of your regret to fuel a sincere apology or a change in behavior.",
                "We all make mistakes; the difference lies in who uses them to become a better person. Your remorse shows that your internal compass is still working.",
                "Don't let regret become your identity. It's a signal to adjust your course, not an anchor to keep you stuck in the past.",
                "Acknowledge the pain you might have caused, but also acknowledge your capacity to heal and make amends. Both are true, and both are necessary.",
                "Note the specific values you feel you compromised. This clarity is a powerful guide for how you will handle similar situations in the future.",
                "Forgiving yourself is just as important as seeking forgiveness from others. You can't move forward effectively if you are constantly looking back.",
                "Remorse is a sign of a functioning conscience. It means you care about the impact you have on the world, which is a noble and vital quality.",
                "Turn your 'I should have' into 'Next time I will.' This shift transforms a heavy emotion into a practical and productive plan for growth.",
                "Be gentle with your past self. You made the best decision you could with the information and emotional resources you had at the time.",
                "The fact that you feel bad is proof that you are not the person who made that mistake anymore. You have already begun to grow beyond it.",
                "Mistakes are the tuition we pay for wisdom. You've paid a high price in remorse; make sure you keep the wisdom you've purchased.",
                "Use this feeling to deepen your empathy for others. Knowing your own capacity for error makes it easier to be kind to others when they fail.",
                "Authentic regret is the first step toward reconciliation. It opens the door for honest communication and the rebuilding of trust.",
                "Don't hide from this feeling, but don't wallow in it either. Walk through it, learn what it has to teach, and then let it go.",
                "You are a work in progress. Every mistake is an opportunity to refine the 'final product' and become a person of greater integrity.",
                "Note that your worth is not defined by your worst moments. You are the sum of your efforts to be better, not just your errors.",
                "Accept the lesson, make the change, and move forward. The world needs the version of you that has learned from this experience."
            ),
            "Surprise" to listOf(
                "The unexpected is a powerful reminder that life is dynamic. Embracing the shock of the new can keep you flexible and foster a resilient mindset.",
                "Whether a surprise is welcome or challenging, it demands your attention and forces you to adapt. This cognitive flexibility is a vital skill.",
                "Taking a moment to process sudden events helps you integrate new information. It is an opportunity to learn about your own reactions.",
                "Surprise is a 'pattern interrupt.' It forces your brain out of its habitual loops and makes you hyper-aware of the present moment.",
                "The suddenness of this moment is a chance to practice agility. Notice how quickly your mind begins to look for a way to respond and adapt.",
                "Shock often creates a brief window of heightened clarity. Use this 'frozen' moment to observe your surroundings with total presence.",
                "A welcome surprise is a gift of dopamine and delight. Savor the feeling of being pleasantly caught off guard by the goodness of life.",
                "A challenging surprise is a test of your foundation. You might feel a bit rattled, but trust your ability to find your footing again quickly.",
                "Note how surprise resets your priorities. In an instant, what was 'important' changes to what is 'necessary' or 'new.'",
                "This moment is proof that you don't have all the answers—and that's okay. The unknown is where the most interesting things happen.",
                "Try to lean into the 'wonder' of surprise rather than the 'fear' of it. The world still has the power to astonish you.",
                "A surprise is an invitation to update your internal map of reality. Something you didn't think was possible has just occurred.",
                "Physical arousal from surprise is neutral. You get to decide if you label it as 'excitement' or 'alarm.' Choose the label that helps you act.",
                "Surprise prevents stagnation. It's the universe's way of shaking the tree to see what falls out and what needs to grow stronger.",
                "Note your immediate instinctual reaction. Does it tell you something about your current level of security or your underlying expectations?",
                "Even 'bad' surprises eventually become part of your story and your strength. They are the plot twists that lead to the most interesting chapters.",
                "The feeling of being 'stunned' is a sign of your brain rapidly reorganizing data. Give yourself a few minutes for the update to complete.",
                "Surprise is the opposite of boredom. Cherish the fact that life can still surprise you; it means you are fully engaged and alive.",
                "Use this shift in energy to take a different approach to your day. The 'old way' of thinking has been interrupted—take advantage of that.",
                "The unexpected often brings hidden opportunities. Look past the shock to see what new doors might have just been blown open."
            ),
            "Neutral" to listOf(
                "There is a quiet and sustainable power in a balanced, steady state of mind. Not every day requires high peaks of emotion; these periods are essential.",
                "Stability is the foundation of long-term emotional well-being. Embracing ordinary moments allows you to observe thoughts without judgment.",
                "In the absence of intense emotion, you have the opportunity to practice mindfulness. This calm baseline provides a clear and grounded perspective.",
                "A quiet day is often a day of profound internal consolidation. This state of neutrality allows your mind to recover from previous intensity.",
                "Neutrality isn't 'nothing'—it’s 'potential.' In this quiet headspace, you have the freedom to choose your next direction with total clarity.",
                "Cherish the calm. These steady periods provide the clarity needed to make the big decisions that intense emotions might otherwise cloud.",
                "Note the absence of friction today. When you aren't fighting an emotion, your mental engine runs at peak efficiency. What will you do with that power?",
                "Steady days are the 'recovery room' for your emotional heart. Enjoy the lack of drama and the simplicity of just being present.",
                "A neutral state is the perfect time for objective self-reflection. Without the 'tint' of a specific mood, you can see your life with perfect honesty.",
                "Productivity often peaks in a neutral state. Without the distraction of high highs or low lows, you can focus on the work with total immersion.",
                "Notice the physical feeling of 'stillness.' Your muscles are relaxed, your breath is steady, and your mind is at ease. This is true equilibrium.",
                "A 'boring' day is actually a safe day. It's a testament to the stability you've built in your environment and your internal world.",
                "In this state, you are the observer of your life rather than the protagonist of a drama. This distance is a powerful tool for wisdom.",
                "Neutrality is the starting line for any new direction. From here, you can move toward joy, focus, or rest with equal ease.",
                "Use this quiet time to tend to the 'small things' you usually ignore when you're busy feeling something intense. Clear the decks for the future.",
                "Steady emotions lead to steady progress. Trust the power of the incremental steps you are taking today while the weather is clear.",
                "This is the 'center' of your emotional compass. Knowing how to find your way back to this neutral point is a vital skill for long-term health.",
                "Let your mind wander without a specific goal. In a neutral state, daydreaming can lead to the most creative and unexpected connections.",
                "Respect the ordinary. The vast majority of a well-lived life happens in these quiet, neutral moments. They are the fabric of your existence.",
                "You are at peace. Acknowledge this lack of conflict as a significant achievement in itself. You have found your balance."
            )
        )

        fun getReflection(text: String, emotion: String): String {
            val lowercaseText = text.lowercase()
            if (crisisKeywords.any { lowercaseText.contains(it) }) {
                return pools["Crisis"]!!.random() + "\n\nHelp is available. Please contact a local helpline or emergency services."
            }
            val pool = pools[emotion] ?: pools["Neutral"]!!
            val coreInsight = pool.random()

            val dynamicPrefix = when (emotion) {
                "Anger", "Annoyance", "Disapproval", "Disgust" -> "I can sense the frustration here, and it's completely valid to feel this way. "
                "Joy", "Excitement", "Amusement" -> "It's wonderful to see you in such a vibrant and positive headspace! "
                "Sadness", "Grief", "Disappointment", "Remorse" -> "I'm sorry things feel heavy right now; please be gentle with yourself today. "
                "Fear", "Nervousness", "Embarrassment" -> "It's natural to feel a bit unsettled by uncertainty, but you are more capable than you think. "
                "Gratitude", "Love", "Admiration", "Caring" -> "There is so much beauty in your perspective; thank you for sharing this warmth. "
                "Confusion", "Curiosity", "Surprise", "Realization" -> "This is such an interesting shift in perspective—it's worth exploring further. "
                "Pride", "Approval", "Relief" -> "You should definitely take a moment to savor this feeling of accomplishment. "
                "Neutral" -> "Taking a moment for a steady, honest check-in is an excellent habit for your mental clarity. "
                else -> "I hear what you're saying, and it's worth looking a little closer at this feeling. "
            }

            return "$dynamicPrefix$coreInsight$disclaimer"
        }
}