package com.sevenlabs.mindsync

object ReflectionEngine {
    private val disclaimer = "\n\n_Note: These AI-generated insights are for personal reflection only and may not always accurately capture your emotional state._"

    private val pools = mapOf(
        "Admiration" to listOf(
            "Recognizing excellence in the world around you is a profound sign of a healthy, expansive mindset. By acknowledging greatness, you are subconsciously aligning your own values with those high standards, which serves as a powerful catalyst for your own personal evolution and the refinement of your character.",
            "Your ability to appreciate others' achievements indicates a secure and mature sense of self. This positive outward focus reduces internal friction and allows you to find inspiration in places others might overlook, effectively turning the world into a roadmap for your own growth and aspirations.",
            "When you feel a deep sense of respect for talent or beauty, you are reinforcing your own capacity for excellence. This state of mind opens you up to new learning opportunities and encourages you to apply that same level of care and dedication to your own personal journey."
        ),
        "Amusement" to listOf(
            "Allowing yourself to lean into levity provides a necessary neurological reset for your nervous system. Even brief moments of humor act as a powerful buffer against the cumulative effects of daily stress, helping you maintain a more resilient, flexible, and balanced perspective on life's challenges.",
            "Finding joy in the absurd or funny moments of life is a vital survival mechanism. It signals to your brain that despite current challenges, safety and pleasure are still accessible, which helps lower cortisol levels and promotes a more sustainable and optimistic long-term outlook.",
            "A sense of humor allows you to distance yourself from immediate stressors, providing a much-needed mental break. It reminds you that while life requires focus and effort, it also contains abundant opportunities for play, connection, and lightheartedness."
        ),
        "Anger" to listOf(
            "This surge of intense energy is frequently a powerful messenger indicating that a personal boundary has been crossed or a core value has been compromised. Acknowledging this heat without immediate reaction allows you to investigate the underlying cause and determine how to protect your peace with firm resolve.",
            "High-intensity frustration can be a productive force if channeled toward constructive problem-solving rather than internal or external conflict. By observing the physical sensations of anger without judgment, you create the mental space necessary to address the situation from a position of calm strength.",
            "When hostility feels overwhelming, it is often a sign of deep-seated exhaustion or a prolonged lack of agency. Prioritizing physical grounding and a change of environment can help lower the immediate intensity, allowing you to eventually articulate your needs with clarity and integrity.",
            "Extreme anger is often a temporary peak of emotional energy that seeks release. Focusing on slow, rhythmic breathing can help bridge the gap between initial reactivity and a more considered, effective response to the situation at hand."
        ),
        "Annoyance" to listOf(
            "Minor irritations are frequently symptoms of mental fatigue or overstimulation in your current environment. Recognizing that your threshold is low allows you to step back and reclaim your mental space before these small triggers accumulate into larger and more difficult emotional burdens.",
            "Irritation serves as a signal to adjust your surroundings or your current focus. Prioritizing a few moments of silence or a change in physical location can often provide the immediate relief needed to restore your patience and internal equilibrium, allowing you to return with a fresh perspective.",
            "When small details feel grating, it is often an indication that your capacity for processing information is currently at its limit. Giving yourself permission to pause and simplify your immediate tasks can help reduce this friction and restore a sense of ease to your day."
        ),
        "Approval" to listOf(
            "The sense of rightness you are feeling suggests a strong alignment between your current actions and your internal moral compass. This internal validation builds a foundation of self-trust that is far more reliable and sustainable than seeking approval from external sources.",
            "When your choices resonate with your values, you experience a significant reduction in mental friction. Embracing this clarity allows you to move forward with increased confidence and a more focused sense of purpose, knowing that you are acting in accordance with your true self.",
            "Feeling a sense of agreement with your circumstances indicates that you have successfully navigated a period of indecision. Take a moment to internalize this feeling of satisfaction, as it reinforces the habits and thought patterns that lead to positive and fulfilling outcomes."
        ),
        "Caring" to listOf(
            "Your capacity for empathy is a profound strength that fosters deep and meaningful human connection. By extending kindness and concern to others, you are actively strengthening the social bonds that provide essential emotional support, both for them and for yourself.",
            "Nurturing others is a testament to the depth of your character, but it is equally vital to direct that same quality of attention inward. Sustainable compassion requires a balanced approach where your own emotional needs and limits are respected alongside those you care for.",
            "The warmth of connection that comes from caring for others provides a sense of belonging and security. This shared emotional experience enriches your life and reminds you that your presence and support have a tangible, positive impact on the world around you."
        ),
        "Confusion" to listOf(
            "Experiencing a state of not knowing is often the necessary precursor to a major cognitive breakthrough or a new level of understanding. This mental fog signifies that you are processing complex variables that haven't yet settled into a recognizable pattern, and sitting with this uncertainty is a valuable mental skill.",
            "Clarity cannot be forced; it emerges naturally as your mind organizes new information over time. Focusing on the simple, certain facts of your immediate environment can provide a sense of stability while the larger picture continues to develop and reveal itself in the background.",
            "When things feel unclear, it is often a sign that you are moving beyond your current knowledge base. Embracing this period of uncertainty as a learning phase allows you to remain open to new insights and avoid the pitfalls of making premature or ill-informed decisions."
        ),
        "Curiosity" to listOf(
            "An inquisitive mind is a resilient one, as it replaces fear of the unknown with a genuine desire for discovery. Following these threads of interest keeps your cognitive faculties sharp and ensures that your perspective on life remains expansive, engaged, and full of possibility.",
            "Your drive to understand the why behind your experiences is a key driver of self-growth and intellectual development. This openness to new information allows you to adapt more quickly to change and find meaningful patterns where others might only see chaos or routine.",
            "Curiosity acts as a natural antidote to stagnation, encouraging you to look beneath the surface of everyday events. This engagement with the world fosters a sense of wonder and ensures that you are constantly evolving and refining your understanding of yourself and others."
        ),
        "Desire" to listOf(
            "Strong longings act as directional signals, highlighting the specific areas where you are most eager for growth, connection, or change. Instead of focusing on the absence of what you want, try to view the desire itself as a source of motivation and energy to pursue your goals.",
            "Acknowledging what you want is an act of fundamental honesty toward yourself. It helps you clarify your priorities and provides a roadmap for your future efforts, ensuring that you are moving toward a life that truly resonates with your deepest and most authentic values.",
            "When you feel a strong pull toward a specific goal or outcome, it is helpful to explore the underlying need it represents. Understanding the core motivation behind your desires can help you find multiple ways to fulfill them and ensure that your efforts lead to lasting satisfaction."
        ),
        "Disappointment" to listOf(
            "It is natural for the mind to feel a sense of loss when reality falls short of deeply held expectations. Allowing yourself the space to process this letdown without the pressure to immediately fix the situation is a vital step in maintaining your emotional health and resilience.",
            "The pain of a missed opportunity is a reflection of your engagement, your hope, and your passion. While the current outcome is not what you sought, the experience contributes to a deeper understanding of your needs and helps you refine your approach for all future endeavors.",
            "Feeling let down is often a sign that you were willing to take a risk for something you valued. Acknowledge the courage it took to try, and give yourself the time needed to regroup before deciding on your next steps with a clearer and more grounded perspective."
        ),
        "Disapproval" to listOf(
            "A sense of disagreement often marks the boundary of your integrity and your personal ethics. It is an important internal exercise that reinforces your commitment to your own standards, ensuring that you remain true to yourself even in challenging or conflicting social situations.",
            "Recognizing when something does not align with your core principles is a key aspect of self-respect. This internal friction serves as a protective mechanism, helping you decide where to place your energy and which paths or behaviors are best avoided to maintain your peace.",
            "Disapproval provides clarity on your values by highlighting what you find unacceptable. By acknowledging this feeling, you reinforce your internal compass, which helps you navigate complex decisions with more consistency and a stronger sense of personal agency."
        ),
        "Disgust" to listOf(
            "Disgust is a primitive but highly effective self-protection mechanism designed to keep you away from toxic or harmful influences. Respecting this visceral reaction allows you to maintain the physical and emotional distance necessary to preserve your health and internal well-being.",
            "When you feel a strong sense of repulsion, it is a clear sign to cleanse your mental or physical space. Moving toward more harmonious, clean, and life-affirming environments is a direct and effective way to restore your sense of comfort and internal equilibrium.",
            "This intense reaction often serves as a powerful boundary that protects your integrity. By acknowledging and honoring your sense of disgust, you are taking a stand for your own well-being and ensuring that you remain in environments that support your growth."
        ),
        "Embarrassment" to listOf(
            "Social self-consciousness is a universal experience that highlights our innate desire for connection and acceptance. In most cases, the intensity of this feeling far exceeds how others perceive the event, and these moments usually fade from collective memory much faster than we expect.",
            "Small mistakes are a natural and unavoidable part of being human. Treating yourself with the same humor and gentleness you would offer to a close friend helps to dissolve the internal tension and allows you to move past the moment with your dignity and confidence intact.",
            "The feeling of being exposed or judged is often a reflection of our own high standards. Reminding yourself that everyone experiences these moments can help reduce the sting and allow you to focus on the connections that truly matter rather than a single awkward event."
        ),
        "Excitement" to listOf(
            "High energy and anticipation are powerful fuels for creativity, innovation, and action. Utilizing this surge of physiological arousal can help you focus more intently on your goals and approach your tasks with a renewed and vibrant sense of possibility and enthusiasm.",
            "Enthusiasm is a vital spark that can transform even the most mundane activities into something extraordinary. Sharing this energy through your actions or your presence can have a positive ripple effect, inspiring those around you and reinforcing your own positive and engaged state.",
            "When you feel a rush of excitement, it is a sign that you are moving toward something that truly resonates with you. Enjoy this peak of energy and let it drive you forward, but also remember to ground yourself so you can make sustainable progress toward your goals."
        ),
        "Fear" to listOf(
            "Fear is a survival mechanism that heightens your awareness of potential risks and prepares your body to protect you. Acknowledging the presence of fear without letting it dictate your actions is a profound and necessary exercise in courage and self-regulation.",
            "During times of uncertainty, your mind naturally searches for threats to ensure your safety. Grounding yourself in the present moment by focusing on slow breathing and physical sensations can help signal to your nervous system that you are safe right now, allowing logic to lead.",
            "When anxiety feels high, it is helpful to break down the situation into smaller, more manageable parts. By addressing one small piece at a time, you regain a sense of agency and reduce the overall feeling of being overwhelmed by things outside of your immediate control.",
            "Intense fear can be a heavy burden to carry alone. Acknowledging the weight of this feeling and allowing yourself to seek out environments or people that provide a sense of security is a vital act of self-care during periods of high stress and uncertainty."
        ),
        "Gratitude" to listOf(
            "Intentionally noticing what is going well creates a neurological shift that improves your overall mood and long-term outlook. This practice of appreciation builds an internal reservoir of positivity that you can draw from during more difficult or challenging times.",
            "Thankfulness is more than just a fleeting feeling; it is a way of interacting with the world that fosters deep connection and resilience. By focusing on the abundance in your life, you are training your brain to see opportunities and support where they might be overlooked.",
            "The warmth of gratitude improves your relationships and fosters a stronger sense of community. Noticing and acknowledging the small, positive details of your day creates a more fulfilling life and reminds you of the many ways you are supported and valued by others."
        ),
        "Grief" to listOf(
            "Deep sorrow is the shadow cast by deep connection, and it requires a great deal of patience, time, and self-gentleness. There is no prescribed timeline for healing, and allowing yourself to simply feel what you feel is an essential and courageous part of the process.",
            "Loss changes our internal landscape, and it takes time and effort to navigate the new terrain. Honor your feelings as a testament to the value of what you have lost, and know that simply existing through the difficult moments is a significant act of endurance.",
            "When despair feels profound, it is important to remember that you do not have to carry the entire weight at once. Focus only on the next few minutes or the next breath, and allow yourself the grace to be imperfect and vulnerable as you navigate this difficult transition.",
            "Extreme sadness often signals a need for deep rest and withdrawal from external pressures. Giving yourself permission to step away from expectations and simply focus on your basic needs is a vital way to preserve your energy while you process your emotions.",
            "Acknowledging deep despair is a step toward eventual healing. While the pain may feel constant, it is often a reflection of the profound love or meaning you held for what was lost. Be patient with your heart as it seeks a new way to move forward."
        ),
        "Joy" to listOf(
            "Radiant happiness is a state that should be fully and intentionally inhabited whenever it appears. Allowing yourself to savor these moments without reservation builds the emotional strength needed to handle life's eventual challenges with more grace and perspective.",
            "Joy often arises from simple, present-moment awareness and the appreciation of the here and now. By cherishing these flashes of delight, you are affirming the beauty of your life and reinforcing the habits that lead to a more fulfilled, balanced, and happy existence.",
            "Feeling a deep sense of well-being provides a powerful counterbalance to life's inevitable stressors. It reminds you of your capacity for happiness and encourages you to continue creating a life that aligns with your passions and brings you a sense of true fulfillment."
        ),
        "Love" to listOf(
            "The experience of deep affection and connection is the cornerstone of a meaningful and fulfilling life. Love provides a sense of belonging and security that allows you to explore the world with more confidence, peace, and a sense of shared human experience.",
            "Whether directed toward a partner, friend, family member, or yourself, love is a transformative force. It encourages growth, fosters patience, and provides a safe and supportive haven from the many complexities and demands of modern life.",
            "Cultivating love requires consistent effort, honesty, and vulnerability, but the rewards are truly immeasurable. It enriches your emotional landscape and provides a profound sense of purpose that can sustain you through the many ups and downs of daily living."
        ),
        "Nervousness" to listOf(
            "Pre-event jitters are frequently a sign that you care deeply about the outcome of what you are about to do. Reframing this surge of nervous energy as readiness or anticipation can help you channel the adrenaline into a more focused and effective performance.",
            "Physical sensations of anxiety can be managed through steady, rhythmic breathing and intentional grounding. Reminding yourself that you have successfully navigated similar feelings in the past helps to restore your confidence and allows you to move forward.",
            "A certain amount of nervous energy is actually helpful for maintaining focus and sharpening your performance. Trust in your preparation and your ability to adapt to the situation, knowing that the intensity will subside once you have begun the task at hand."
        ),
        "Optimism" to listOf(
            "Maintaining a hopeful outlook is a proactive strategy for mental well-being and long-term success. It allows you to see potential solutions where others might only see obstacles and helps you persist through periods of difficulty with a sense of purpose.",
            "Optimism is not about ignoring the reality of a situation, but about choosing to focus on the possibilities for positive change and growth. This mindset encourages you to take the necessary steps toward a future that aligns with your goals and your values.",
            "Your belief in a better tomorrow acts as a powerful motivator by influencing your current actions and your overall energy. It attracts more positive opportunities and helps you build a more resilient, happy, and engaged life for yourself and others."
        ),
        "Pride" to listOf(
            "Taking ownership of your achievements and your hard work is a vital part of building a healthy and robust sense of self-regard. You have earned the right to acknowledge your persistence and the effort that led to your success, so let that feeling strengthen you.",
            "Feeling proud of your personal growth and your progress reinforces your belief in your own capabilities. This self-recognition provides the necessary internal motivation to tackle even more ambitious and meaningful challenges in the future.",
            "Internalizing your successes helps you build a more stable and effective sense of identity. It reminds you that you are a capable individual who can achieve significant goals through dedication, focus, and a commitment to continuous improvement."
        ),
        "Realization" to listOf(
            "A sudden shift in perspective or a new insight can clarify a long-standing problem and provide a clear, new path forward. These insights are the milestones of your personal growth, helping you to shed old habits and approach your life with more intention.",
            "When clarity strikes, it fundamentally changes your relationship with your circumstances. Taking a moment to fully integrate this new understanding allows you to move forward with a more accurate, helpful, and empowered view of your own potential.",
            "The moments where everything clicks are rare and valuable. Use this new-found clarity to simplify your priorities and focus your energy on the actions that will lead to the most meaningful and positive changes in your daily life and overall well-being."
        ),
        "Relief" to listOf(
            "The ending of a period of prolonged tension or stress offers a vital opportunity for mental and physical recovery. Allow yourself to fully experience the lightness that comes with resolution, and use this quiet time to recharge your emotional energy.",
            "Take a deep breath and appreciate the mental space that has opened up now that a significant burden has been lifted. This transition is a powerful reminder of your ability to endure difficult phases and emerge into a state of peace and calm.",
            "The stillness that follows a resolved challenge is a valuable time for rejuvenation. Acknowledge the effort it took to reach this point and give yourself permission to simply exist in the quiet without the pressure of the next immediate demand."
        ),
        "Remorse" to listOf(
            "Feeling regret is a strong indicator of your moral health and your desire to act in accordance with your values. Instead of dwelling on the past, focus on the lessons learned and the steps you can take now to make amends and grow from the experience.",
            "Self-forgiveness is a necessary part of the human learning process. Acknowledge your mistake with honesty, understand the factors that led to it, and then commit to a different path forward, allowing the remorse to transform into wisdom.",
            "While uncomfortable, remorse provides the friction necessary for meaningful change. By investigating the source of your regret, you gain a clearer understanding of your ethics and can build a future that is more in line with the person you want to be."
        ),
        "Sadness" to listOf(
            "Melancholy is a quiet and reflective state that often arises when we are processing change, disappointment, or loss. It is a valid part of the human experience that requires patience and self-compassion rather than an immediate or forced fix.",
            "Allowing yourself to feel sad without judgment can lead to deeper insights about what truly matters to you. It is a slow, inward-facing emotion that encourages you to take a break from external demands and care for your internal world.",
            "During low periods, engaging in simple, comforting activities can provide a much-needed sense of safety and ease. Remember that all emotional states are temporary and that your internal weather will eventually shift back toward a state of balance.",
            "When the weight of the day feels heavy, it is important to acknowledge that you are doing your best. Emotional exhaustion is real, and giving yourself the grace to be less than perfect is a vital part of maintaining your long-term well-being.",
            "Feeling down is an invitation to practice radical self-kindness. Listen to what your heart needs—whether it is rest, silence, or a small comfort—and allow yourself the space to be exactly where you are without any pressure to change."
        ),
        "Surprise" to listOf(
            "The unexpected is a powerful reminder that life is dynamic and unpredictable. Embracing the shock of the new can keep you flexible and open to experiences that fall outside of your usual routine, fostering a more resilient and engaged mindset.",
            "Whether a surprise is welcome or challenging, it demands your full attention and forces you to adapt quickly. This cognitive flexibility is a vital skill for navigating a complex world and finding opportunities in the midst of sudden change.",
            "Taking a moment to process sudden news or events helps you integrate the new information into your worldview. It is an opportunity to learn something new about your own reactions and how you handle the shifting nature of your environment."
        ),
        "Neutral" to listOf(
            "There is a quiet and sustainable power in a balanced, steady state of mind. Not every day requires high peaks of emotion; these periods of neutrality are essential for resting your nervous system and integrating the lessons of your experiences.",
            "Stability is the foundation of long-term emotional well-being. Embracing the ordinary moments of your day allows you to observe your thoughts without judgment, fostering a deep, sustainable, and peaceful sense of internal equilibrium.",
            "In the absence of intense emotion, you have the opportunity to practice mindfulness and pure observation. This calm baseline allows you to see things as they are, providing a clear and grounded perspective that supports wise decision-making.",
            "A quiet day is often a day of profound internal consolidation. While it may feel unremarkable, this state of neutrality allows your mind to recover from previous intensity and prepare for whatever experiences the future may bring."
        )
    )

    fun getReflection(emotion: String): String {
        val pool = pools[emotion] ?: pools["Neutral"]!!
        return pool.random() + disclaimer
    }
}