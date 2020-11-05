const {utils, URLParams, helpers, config, hooks} = service;

return new Promise(async resolve => {
    window.addEventListener('message', async event => {
        if (event.data == '') return

        console.log('[VQ] message received', event);

        const data = JSON.parse(event.data);

        if (data.eventName && data.eventName == 'useLocals' && data.locals) {

            if (data.locals.error) {
                helpers.raiseError({message: 'Could not get data'});
                return resolve();
            }

            const [person1, person2, person3] = data.locals.payload;

            await helpers.useLocals({
                name1: person1.first_name + ' ' + person1.last_name,
                image1: person1.avatar,
                name2: person2.first_name + ' ' + person2.last_name,
                image2: person2.avatar,
                name3: person3.first_name + ' ' + person3.last_name,
                image3: person3.avatar,
            });
            resolve();
        }

    });

    console.log('ready sent');
    window.parent.postMessage('ready', '*');
});


